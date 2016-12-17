package net.clgd.ccemux.emulation;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ILuaAPI;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.RenderingMethod;

@FunctionalInterface
interface APIMethod {
	Object[] accept(Object[] o) throws LuaException;
}

public class CCEmuXAPI implements ILuaAPI {

	private String name;
	private EmulatedComputer computer;
	private final Map<String, APIMethod> methods = new LinkedHashMap<>();

	{
		methods.put("getVersion", o -> new Object[] {CCEmuX.getVersion()});

		methods.put("setCursorChar", o -> {
			if (o.length < 1 || !(o[0] instanceof String)) {
				throw new LuaException("expected string for argument #1");
			}

			String s = (String) o[0];
			if (s.length() < 1) {
				throw new LuaException("cursor char cannot be empty");
			}

			computer.cursorChar = s.charAt(0);

			return new Object[] {};
		});

		methods.put("closeEmu", o -> {
			computer.dispose();
			return new Object[] {};
		});

		methods.put("openEmu", o -> {
			int id = -1;

			if (o.length > 0 && o[0] != null) {
				if (o[0] instanceof Number) {
					id = ((Number)o[0]).intValue();
				} else {
					throw new LuaException("expected number or nil for argument #1");
				}
			}

			EmulatedComputer ec = computer.emu.createEmulatedComputer(id, null);
			List<Renderer> r = ec.emu.conf.getRenderer().stream()
					.map(s -> RenderingMethod.create(s, ec))
					.collect(Collectors.toList());

			r.forEach(_r -> _r.setVisible(true));

			return new Object[] {ec.getID()};
		});

		methods.put("openDataDir", o -> {
			try {
				Desktop.getDesktop().browse(computer.emu.dataDir.toUri());
				return new Object[] {true};
			} catch (Exception e) {
				return new Object[] {false, e.toString()};
			}
		});

		methods.put("milliTime", o -> {
			return new Object[] {System.currentTimeMillis()};
		});

		methods.put("nanoTime", o -> {
			return new Object[] {System.nanoTime()};
		});

		methods.put("echo", o -> {
			if (o.length > 0 && o[0] instanceof String) {
				computer.emu.logger.info("[Computer {}] {}", computer.getID(), (String)o[0]);
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
	}

	public CCEmuXAPI(EmulatedComputer computer, String name) {
		this.computer = computer;
		this.name = name;
	}

	@Override
	public String[] getMethodNames() {
		return methods.keySet().toArray(new String[] {});
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] arguments)
			throws LuaException, InterruptedException {
		return new ArrayList<APIMethod>(methods.values()).get(method).accept(arguments);
	}

	@Override
	public void advance(double arg0) {
	}

	@Override
	public String[] getNames() {
		return new String[]{name};
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void startup() {
	}

}
