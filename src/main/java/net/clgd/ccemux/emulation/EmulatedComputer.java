package net.clgd.ccemux.emulation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.terminal.Terminal;
import net.clgd.ccemux.emulation.tror.TRoRTerminal;

public class EmulatedComputer extends Computer {
	public static interface Listener {
		public void onAdvance(double dt);
		public void onDispose();
		public void onTerminalResized(int width, int height);
	}
	
	private static final Field termField;
	
	static {
		Field f;
		
		try {
			f = EmulatedComputer.class.getSuperclass().getDeclaredField("m_terminal");
			f.setAccessible(true);
		} catch (SecurityException | NoSuchFieldException e) {
			f = null;
		}
		
		termField = f;
	}
	
	public final CCEmuX emu;
	public final Terminal terminal;
	public char cursorChar = '_';
	
	private final List<Listener> listeners = new ArrayList<>();
	
	EmulatedComputer(CCEmuX emu, int termWidth, int termHeight, int id) {
		super(emu.env, new TRoRTerminal(emu, termWidth, termHeight), -1);
		
		this.emu = emu;
		
		Terminal term;
		try {
			term = (Terminal) termField.get(this);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			term = null;
		}
		this.terminal = term;
		
		if (emu.conf.isApiEnabled()) addAPI(new CCEmuXAPI(this, "ccemux"));
	}
	
	public boolean addListener(Listener l) {
		return listeners.add(l);
	}
	
	public boolean removeListener(Listener l) {
		return listeners.remove(l);
	}
	
	public void dispose() {
		shutdown();
		
		listeners.forEach(l -> l.onDispose());
		
		emu.removeEmulatedComputer(this);
	}
	
	@Override
	public void advance(double dt) {
		super.advance(dt);
		
		listeners.forEach(l -> l.onAdvance(dt));
	}
	
	public void pressKey(int keycode, boolean release) {
		queueEvent(release ? "key_up" : "key", new Object[] {keycode});
	}
	
	public void pressChar(char c) {
		queueEvent("char", new Object[] {"" + c});
	}
	
	public void paste(String text) {
		queueEvent("paste", new Object[] {text});
	}
	
	public void terminate() {
		queueEvent("terminate", new Object[] {});
	}
	
	public void click(int button, int x, int y, boolean release) {
		queueEvent(release ? "mouse_up" : "mouse_click", new Object[] {button, x, y});
	}
	
	public void drag(int button, int x, int y) {
		queueEvent("mouse_drag", new Object[] {button, x, y});
	}
	
	public void scroll(int dir, int x, int y) {
		queueEvent("mouse_scroll", new Object[] {dir, x, y});
	}
}
