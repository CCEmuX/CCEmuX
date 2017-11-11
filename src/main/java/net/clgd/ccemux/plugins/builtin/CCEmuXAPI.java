package net.clgd.ccemux.plugins.builtin;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.IOUtils;

import com.google.auto.service.AutoService;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ILuaAPI;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.emulation.filesystem.VirtualDirectory.Builder;
import net.clgd.ccemux.emulation.filesystem.VirtualFile;
import net.clgd.ccemux.plugins.Plugin;
import net.clgd.ccemux.plugins.hooks.ComputerCreated;
import net.clgd.ccemux.plugins.hooks.CreatingROM;

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

		public API(CCEmuX emu, EmulatedComputer computer, String name) {
			this.name = name;

			methods.put("getVersion", o -> new Object[] { CCEmuX.getVersion() });

			methods.put("closeEmu", o -> {
				computer.shutdown();
				emu.removeComputer(computer);
				return new Object[] {};
			});

			methods.put("openEmu", o -> {
				int id;

				if (o.length > 0 && o[0] != null) {
					if (o[0] instanceof Number) {
						id = ((Number) o[0]).intValue();
					} else {
						throw new LuaException("expected number or nil for argument #1");
					}
				} else {
					id = -1;
				}

				EmulatedComputer ec = emu.createComputer(b -> b.id(id));

				return new Object[] { ec.getID() };
			});

			methods.put("openDataDir", o -> {
				try {
					Desktop.getDesktop().browse(emu.getCfg().getDataDir().toUri());
					return new Object[] { true };
				} catch (Exception e) {
					return new Object[] { false, e.toString() };
				}
			});

			methods.put("milliTime", o -> new Object[] { System.currentTimeMillis() });
			methods.put("nanoTime", o -> new Object[] { System.nanoTime() });

			methods.put("echo", o -> {
				if (o.length > 0 && o[0] instanceof String) {
					log.info("[Computer {}] {}", computer.getID(), (String) o[0]);
				} else {
					throw new LuaException("expected string for argument #1");
				}

				return new Object[] {};
			});

			methods.put("setClipboard", o -> {
				if (o.length > 0 && o[0] instanceof String) {
					StringSelection sel = new StringSelection((String) o[0]);
					Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
					c.setContents(sel, sel);
				} else {
					throw new LuaException("expected string for argument #1");
				}

				return new Object[] {};
			});

			methods.put("openConfig", o -> {
				if (emu.getRendererFactory().createConfigEditor(emu.getCfg())) {
					return new Object[]{true};
				} else {
					return new Object[]{false, "Not supported with this renderer"};
				}
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
	public void setup(EmuConfig cfg) {
		registerHook(new ComputerCreated() {
			@Override
			public void onComputerCreated(CCEmuX emu, EmulatedComputer computer) {
				computer.addAPI(new API(emu, computer, "ccemux"));
			}
		});

		registerHook(new CreatingROM() {
			@Override
			public void onCreatingROM(CCEmuX emu, Builder romBuilder) {
				try {
					romBuilder.addEntry(Paths.get("programs/emu.lua"), new VirtualFile(
							IOUtils.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_program.lua"))));

					romBuilder.addEntry(Paths.get("help/emu.txt"), new VirtualFile(
							IOUtils.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_help.txt"))));
				} catch (IOException e) {
					log.error("Failed to register ROM entries", e);
				}
			}
		});
	}
}
