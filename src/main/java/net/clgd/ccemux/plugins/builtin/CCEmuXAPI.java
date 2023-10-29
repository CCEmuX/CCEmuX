package net.clgd.ccemux.plugins.builtin;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.text.html.Option;

import dan200.computercraft.core.apis.TableHelper;
import dan200.computercraft.core.util.ArgumentHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.core.computer.ComputerSide;
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

	public static class API implements ILuaAPI {
		private final Emulator emu;
		private final EmulatedComputer computer;

		public API(Emulator emu, EmulatedComputer computer) {
			this.emu = emu;
			this.computer = computer;
		}

		@LuaFunction
		public final String getVersion() {
			return emu.getEmulatorVersion();
		}

		@LuaFunction
		public final void closeEmu() {
			computer.shutdown();
			emu.removeComputer(computer);
		}

		private record TermSize(int width, int height) { }

		@LuaFunction
		public final int openEmu(Optional<Integer> id, Optional<Map<?, ?>> properties) throws LuaException {
			var props = properties.orElse(Collections.emptyMap());
			TermSize termSize = null;
			if (props.containsKey("width") || props.containsKey("height")) {
				termSize = new TermSize(
					TableHelper.getIntField(props, "width"),
					TableHelper.getIntField(props, "height")
				);
			}

			Optional<Double> termScale = TableHelper.optRealField(props, "scale");

			return createComputer(id, termSize, termScale);
		}

		private int createComputer(Optional<Integer> id, @Nullable TermSize termSize, Optional<Double> termScale) {
			return emu.createComputer(b -> {
				b.id(id.orElse(-1));
				if (termSize != null) b.termSize(termSize.width(), termSize.height());
				termScale.ifPresent(b::termScale);
			}).getID();
		}

		@LuaFunction
		public final Object[] openDataDir(Optional<Integer> id) {
			try {
				Path path = id.isPresent() ? emu.getConfig().getComputerDir(id.get()) : emu.getConfig().getDataDir();
				if (!Files.isDirectory(path)) return new Object[] { false, "Directory does not exist." };

				Desktop.getDesktop().browse(path.toUri());
				return new Object[] { true };
			} catch (Exception e) {
				return new Object[] { false, e.toString() };
			}
		}

		@LuaFunction
		public final long milliTime() {
			return System.currentTimeMillis();
		}

		@LuaFunction
		public final long nanoTime() {
			return System.nanoTime();
		}

		@LuaFunction
		public final void echo(String message) {
			log.info("[Computer {}] {}", computer.getID(), message);
		}

		@LuaFunction
		public final void setClipboard(String contents) {
			StringSelection sel = new StringSelection(contents);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
		}

		@LuaFunction
		public final Object[] openConfig() {
			if (emu.getRendererFactory().createConfigEditor(emu.getConfig())) {
				return new Object[] { true };
			} else {
				return new Object[] { false, "Not supported with this renderer" };
			}
		}

		@LuaFunction
		public final void attach(IArguments arguments) throws LuaException {
			ComputerSide side = arguments.getEnum(0, ComputerSide.class);
			String peripheral = arguments.getString(1);
			Object configuration = arguments.get(2);

			PeripheralFactory<?> factory = emu.getPeripheralFactory(peripheral);
			if (factory == null) throw new LuaException("Invalid peripheral");
			Peripheral built = factory.create(computer, emu.getConfig());

			// Setup the config for this peripheral. In the future this could be
			// persisted to disk.
			Group group = new Group("peripheral");
			built.configSetup(group);
			if (configuration != null) LuaAdapter.fromLua(group, configuration);

			computer.getEnvironment().setPeripheral(side, built);
		}

		@LuaFunction
		public final void detach(ComputerSide side) {
			computer.getEnvironment().setPeripheral(side, null);
		}

		@LuaFunction
		public final MethodResult screenshot() {
			return Utils.awaitFuture(computer, computer.screenshot(),
				f -> MethodResult.of(f.getName()),
				e -> {
					log.error("Cannot create screenshot", e);
					return MethodResult.of(null, "Cannot create screenshot.");
				});
		}

		@Override
		public String[] getNames() {
			return new String[] { "ccemux" };
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
		registerHook((ComputerCreated) (emu, computer) -> computer.addApi(new API(emu, computer)));

		registerHook((CreatingROM) (emu, romBuilder) -> {
			try {
				romBuilder.addEntry("programs/emu.lua", new VirtualFile(
					ByteStreams.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_program.lua"))));

				romBuilder.addEntry("help/emu.txt", new VirtualFile(
					ByteStreams.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_help.txt"))));

				romBuilder.addEntry("help/credits-emu.md", new VirtualFile(
					ByteStreams.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/credits_help.md"))));

				romBuilder.addEntry("autorun/emu.lua", new VirtualFile(
					ByteStreams.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_completion.lua"))));
			} catch (IOException e) {
				log.error("Failed to register ROM entries", e);
			}
		});
	}
}
