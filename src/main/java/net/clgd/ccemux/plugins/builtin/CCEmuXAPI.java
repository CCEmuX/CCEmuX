package net.clgd.ccemux.plugins.builtin;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.*;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ArgumentHelper;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.ComputerThread;
import net.clgd.ccemux.Utils;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.emulation.Emulator;
import net.clgd.ccemux.api.emulation.filesystem.VirtualFile;
import net.clgd.ccemux.api.peripheral.Peripheral;
import net.clgd.ccemux.api.peripheral.PeripheralFactory;
import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.PluginManager;
import net.clgd.ccemux.api.plugins.hooks.ComputerCreated;
import net.clgd.ccemux.api.plugins.hooks.CreatingROM;
import net.clgd.ccemux.config.LuaAdapter;

@AutoService(Plugin.class)
public class CCEmuXAPI extends Plugin {
	private static final Logger log = LoggerFactory.getLogger(CCEmuXAPI.class);

	@FunctionalInterface
	private interface APIMethod {
		Object[] accept(ILuaContext c, Object[] o) throws LuaException, InterruptedException;
	}

	private static class API implements ILuaAPI {
		private static final List<String> sideNames = Arrays.asList(Computer.s_sideNames);

		private final String name;
		private final Map<String, APIMethod> methods = new LinkedHashMap<>();

		public API(Emulator emu, EmulatedComputer computer, String name) {
			this.name = name;

			methods.put("getVersion", (c, o) -> new Object[] { emu.getEmulatorVersion() });

			methods.put("closeEmu", (c, o) -> {
				computer.shutdown();
				emu.removeComputer(computer);
				return new Object[] {};
			});

			methods.put("openEmu", (c, o) -> {
				int id = ArgumentHelper.optInt(o, 0, -1);
				EmulatedComputer ec = emu.createComputer(b -> b.id(id));

				return new Object[] { ec.getID() };
			});

			methods.put("openDataDir", (c, o) -> {
				try {
					Desktop.getDesktop().browse(emu.getConfig().getDataDir().toUri());
					return new Object[] { true };
				} catch (Exception e) {
					return new Object[] { false, e.toString() };
				}
			});

			methods.put("milliTime", (c, o) -> new Object[] { System.currentTimeMillis() });
			methods.put("nanoTime", (c, o) -> new Object[] { System.nanoTime() });

			methods.put("echo", (c, o) -> {
				String message = ArgumentHelper.getString(o, 0);
				log.info("[Computer {}] {}", computer.getID(), message);

				return new Object[] {};
			});

			methods.put("setClipboard", (c, o) -> {
				String contents = ArgumentHelper.getString(o, 0);
				StringSelection sel = new StringSelection(contents);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);

				return new Object[] {};
			});

			methods.put("openConfig", (c, o) -> {
				if (emu.getRendererFactory().createConfigEditor(emu.getConfig())) {
					return new Object[] { true };
				} else {
					return new Object[] { false, "Not supported with this renderer" };
				}
			});

			methods.put("attach", (c, o) -> {
				String side = ArgumentHelper.getString(o, 0);
				String peripheral = ArgumentHelper.getString(o, 1);
				Object configuration = o.length > 2 ? o[2] : null;

				int sideId = sideNames.indexOf(side);
				if (sideId == -1) throw new LuaException("Invalid side");

				PeripheralFactory<?> factory = emu.getPeripheralFactory(peripheral);
				if (factory == null) throw new LuaException("Invalid peripheral");
				Peripheral built = factory.create(computer, emu.getConfig());

				// Setup the config for this peripheral. In the future this could be
				// persisted to disk.
				Group group = new Group("peripheral");
				built.configSetup(group);
				if (configuration != null) LuaAdapter.fromLua(group, configuration);

				computer.setPeripheral(sideId, built);
				awaitPeripheralChange(computer, c);

				return null;
			});

			methods.put("detach", (c, o) -> {
				String side = ArgumentHelper.getString(o, 0);
				int sideId = sideNames.indexOf(side);
				if (sideId == -1) throw new LuaException("Invalid side");

				computer.setPeripheral(sideId, null);
				awaitPeripheralChange(computer, c);

				return null;
			});

			methods.put("screenshot", (c, o) -> Utils.awaitFuture(computer, c, computer.screenshot(),
				f -> new Object[] { f.getName() },
				e -> {
					log.error("Cannot create screenshot", e);
					return new Object[] { null, "Cannot create screenshot." };
				}));
		}

		@Nonnull
		@Override
		public String[] getMethodNames() {
			return methods.keySet().toArray(new String[] {});
		}

		@Override
		public Object[] callMethod(@Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
			return new ArrayList<>(methods.values()).get(method).accept(context, arguments);
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

		/**
		 * Waits for peripherals to finish attaching/detaching.
		 *
		 * Peripherals are attached/detached on the {@link ComputerThread}, which means they won't have been properly
		 * changed after {@code ccemux.attach}/{@code ccemux.detach} has been called. Instead, we queue an event on the
		 * computer thread, so we are resumed after peripherals have actually been attached.
		 *
		 * @param computer The computer to queue an event on
		 * @param context  The context to pull an event from
		 * @throws InterruptedException If pulling an event failed
		 */
		static void awaitPeripheralChange(EmulatedComputer computer, ILuaContext context) throws InterruptedException {
			computer.queueEvent("ccemux_peripheral_update", null);
			context.pullEventRaw("ccemux_peripheral_update");
		}
	}

	@Nonnull
	@Override
	public String getName() {
		return "CCEmuX API";
	}

	@Nonnull
	@Override
	public String getDescription() {
		return "Adds the 'ccemux' Lua API, which adds methods for interacting with the emulator and real computer.\n"
			+ "Also adds the `emu` program and help ROM files, which use the API.";
	}

	@Nonnull
	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Collection<String> getAuthors() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void setup(@Nonnull PluginManager manager) {
		registerHook((ComputerCreated) (emu, computer) -> computer.addAPI(new API(emu, computer, "ccemux")));

		registerHook((CreatingROM) (emu, romBuilder) -> {
			try {
				romBuilder.addEntry("programs/emu.lua", new VirtualFile(
					ByteStreams.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_program.lua"))));

				romBuilder.addEntry("help/emu.txt", new VirtualFile(
					ByteStreams.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_help.txt"))));

				romBuilder.addEntry("help/credits-emu.txt", new VirtualFile(
					ByteStreams.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/credits_help.txt"))));

				romBuilder.addEntry("autorun/emu.lua", new VirtualFile(
					ByteStreams.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_completion.lua"))));
			} catch (IOException e) {
				log.error("Failed to register ROM entries", e);
			}
		});
	}
}
