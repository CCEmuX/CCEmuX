package net.clgd.ccemux.plugins.builtin;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import dan200.computercraft.api.filesystem.IWritableMount;
import lombok.val;
import org.apache.commons.io.IOUtils;

import com.google.auto.service.AutoService;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ArgumentHelper;
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

			methods.put("getVersion", o -> new Object[]{CCEmuX.getVersion()});

			methods.put("closeEmu", o -> {
				computer.shutdown();
				emu.removeComputer(computer);
				return new Object[]{};
			});

			methods.put("openEmu", o -> {
				int id = ArgumentHelper.optInt(o, 0, -1);
				String program = ArgumentHelper.optString(o, 1, null);
				
				EmulatedComputer ec = emu.createComputer(b -> b.id(id), program == null);
				
				if (program != null) {
					IWritableMount mount = ec.getRootMount();
					
					try {
						if (!mount.exists(program)) {
							emu.removeComputer(ec);
							throw new LuaException("program not found");
						}
						
						if (!mount.isDirectory("startup/")) mount.makeDirectory("startup");
						
						val src = mount.openForRead(program);
						val dst = mount.openForWrite("startup/0-ccemux.lua");
						IOUtils.copy(src, dst);
						src.close();
						dst.close();
					} catch (IOException e) {
						emu.removeComputer(ec);
						return new Object[] { false, e.toString() };
					}
					
					ec.turnOn();
				}

				return new Object[]{ec.getID()};
			});

			methods.put("openDataDir", o -> {
				try {
					Desktop.getDesktop().browse(emu.getCfg().getDataDir().toUri());
					return new Object[]{true};
				} catch (Exception e) {
					return new Object[]{false, e.toString()};
				}
			});

			methods.put("milliTime", o -> new Object[]{System.currentTimeMillis()});
			methods.put("nanoTime", o -> new Object[]{System.nanoTime()});

			methods.put("echo", o -> {
				String message = ArgumentHelper.getString(o, 0);
				log.info("[Computer {}] {}", computer.getID(), message);

				return new Object[]{};
			});

			methods.put("setClipboard", o -> {
				String contents = ArgumentHelper.getString(o, 0);
				StringSelection sel = new StringSelection(contents);
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				c.setContents(sel, sel);

				return new Object[]{};
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
			return methods.keySet().toArray(new String[]{});
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
			return new String[]{name};
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

					romBuilder.addEntry(Paths.get("autorun/emu.lua"), new VirtualFile(
							IOUtils.toByteArray(CCEmuXAPI.class.getResourceAsStream("/rom/emu_completion.lua"))));
				} catch (IOException e) {
					log.error("Failed to register ROM entries", e);
				}
			}
		});
	}
}
