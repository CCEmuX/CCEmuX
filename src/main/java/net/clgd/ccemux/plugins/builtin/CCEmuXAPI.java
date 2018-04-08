package net.clgd.ccemux.plugins.builtin;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.auto.service.AutoService;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ArgumentHelper;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.core.computer.Computer;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.emulation.Emulator;
import net.clgd.ccemux.api.emulation.filesystem.VirtualDirectory.Builder;
import net.clgd.ccemux.api.emulation.filesystem.VirtualFile;
import net.clgd.ccemux.api.peripheral.Peripheral;
import net.clgd.ccemux.api.peripheral.PeripheralFactory;
import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.PluginManager;
import net.clgd.ccemux.api.plugins.hooks.ComputerCreated;
import net.clgd.ccemux.api.plugins.hooks.CreatingROM;
import net.clgd.ccemux.config.LuaAdapter;

@Slf4j
@AutoService(Plugin.class)
public class CCEmuXAPI extends Plugin {
	@FunctionalInterface
	private interface APIMethod {
		Object[] accept(Object[] o) throws LuaException;
	}

	private static class API implements ILuaAPI {
		private final String name;
		private final Map<String, APIMethod> methods = new LinkedHashMap<>();

		public API(Emulator emu, EmulatedComputer computer, String name) {
			this.name = name;

			methods.put("getVersion", o -> new Object[] { emu.getEmulatorVersion() });

			methods.put("closeEmu", o -> {
				computer.shutdown();
				emu.removeComputer(computer);
				return new Object[] {};
			});

			methods.put("openEmu", o -> {
				int id = ArgumentHelper.optInt(o, 0, -1);
				EmulatedComputer ec = emu.createComputer(b -> b.id(id));

				return new Object[] { ec.getID() };
			});

			methods.put("openDataDir", o -> {
				try {
					Desktop.getDesktop().browse(emu.getConfig().getDataDir().toUri());
					return new Object[] { true };
				} catch (Exception e) {
					return new Object[] { false, e.toString() };
				}
			});

			methods.put("milliTime", o -> new Object[] { System.currentTimeMillis() });
			methods.put("nanoTime", o -> new Object[] { System.nanoTime() });

			methods.put("echo", o -> {
				String message = ArgumentHelper.getString(o, 0);
				log.info("[Computer {}] {}", computer.getID(), message);

				return new Object[] {};
			});

			methods.put("setClipboard", o -> {
				String contents = ArgumentHelper.getString(o, 0);
				StringSelection sel = new StringSelection(contents);
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				c.setContents(sel, sel);

				return new Object[] {};
			});

			methods.put("openConfig", o -> {
				if (emu.getRendererFactory().createConfigEditor(emu.getConfig())) {
					return new Object[] { true };
				} else {
					return new Object[] { false, "Not supported with this renderer" };
				}
			});

			methods.put("attach", o -> {
				String side = ArgumentHelper.getString(o, 0);
				String peripheral = ArgumentHelper.getString(o, 1);
				Object configuration = o.length > 2 ? o[2] : null;

				int sideId = ArrayUtils.indexOf(Computer.s_sideNames, side);
				if (sideId == -1) throw new LuaException("Invalid side");

				PeripheralFactory<?> factory = emu.getPeripheralFactory(peripheral);
				if(factory == null) throw new LuaException("Invalid peripheral");
				Peripheral built = factory.create(computer, emu.getConfig());

				// Setup the config for this peripheral. In the future this could be
				// persisted to disk.
				Group group = new Group("peripheral");
				built.configSetup(group);
				if (configuration != null) LuaAdapter.fromLua(group, configuration);

				computer.setPeripheral(sideId, built);

				return null;
			});

			methods.put("detach", o -> {
				String side = ArgumentHelper.getString(o, 0);
				int sideId = ArrayUtils.indexOf(Computer.s_sideNames, side);
				if (sideId == -1) throw new LuaException("Invalid side");

				computer.setPeripheral(sideId, null);
				return null;
			});
		}

		@Override
		public String[] getMethodNames() {
			return methods.keySet().toArray(new String[] {});
		}

		@Override
		public Object[] callMethod(ILuaContext context, int method, Object[] arguments)
				throws LuaException, InterruptedException {
			return new ArrayList<>(methods.values()).get(method).accept(arguments);
		}

		@Override
		public void advance(double dt) {}

		@Override
		public String[] getNames() {
			return new String[] { name };
		}

		@Override
		public void shutdown() {}

		@Override
		public void startup() {}
	}

	@Override
	public String getName() {
		return "CCEmuX API";
	}

	@Override
	public String getDescription() {
		return "Adds the 'ccemux' Lua API, which adds methods for interacting with the emulator and real computer.\n"
				+ "Also adds the `emu` program and help ROM files, which use the API.";
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Override
	public Collection<String> getAuthors() {
		return Collections.emptyList();
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void setup(PluginManager manager) {
		registerHook(new ComputerCreated() {
			@Override
			public void onComputerCreated(Emulator emu, EmulatedComputer computer) {
				computer.addAPI(new API(emu, computer, "ccemux"));
			}
		});

		registerHook(new CreatingROM() {
			@Override
			public void onCreatingROM(Emulator emu, Builder romBuilder) {
				try {
					romBuilder.addEntry(Paths.get("programs/emu.lua"), new VirtualFile(
							IOUtils.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_program.lua"))));

					romBuilder.addEntry(Paths.get("help/emu.txt"), new VirtualFile(
							IOUtils.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_help.txt"))));

					romBuilder.addEntry(Paths.get("autorun/emu.lua"), new VirtualFile(
							IOUtils.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_completion.lua"))));
				} catch (IOException e) {
					log.error("Failed to register ROM entries", e);
				}
			}
		});
	}
}
