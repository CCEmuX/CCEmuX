package net.clgd.ccemux.rendering.tror;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.emulation.tror.CapabilitiesPacket;
import net.clgd.ccemux.emulation.tror.ConnectionClosedPacket;
import net.clgd.ccemux.emulation.tror.ResizePacket;
import net.clgd.ccemux.emulation.tror.TRoRPacket;
import net.clgd.ccemux.emulation.tror.TRoRTerminal;
import net.clgd.ccemux.rendering.Renderer;

public class TRoRRenderer implements Renderer {
	public static final Pattern eventPattern = Pattern.compile("^EV:([^;]*);(.*)$");
	public static final HashMap<Pattern, BiConsumer<EmulatedComputer, MatchResult>> handlers = new HashMap<>();
	
	static {
		// char
		handlers.put(Pattern.compile("^\"char\",\"(.)\""), (ec, m) -> ec.pressChar(m.group(1).charAt(0)));
		
		// key
		handlers.put(Pattern.compile("^\"key\",(\\d+)"), (ec, m) -> ec.pressKey(Integer.parseInt(m.group(1)), false));
		
		// key_up
		handlers.put(Pattern.compile("^\"key_up\",(\\d+)"), (ec, m) -> ec.pressKey(Integer.parseInt(m.group(1)), true));
		
		// paste
		handlers.put(Pattern.compile("^\"paste\",\"(.+)\""), (ec, m) -> ec.paste(m.group(1)));
	}
	
	public static MatchResult matches(String s, Pattern p) {
		Matcher m = p.matcher(s);
		
		return m.matches() ? m.toMatchResult() : null;
	}
	
	private final EmulatedComputer ec;
	private final TRoRTerminal term;
	private final OutputStream output;
	private final AsyncScanner input;
	
	private boolean firstsend = true;
	private boolean paused = true;
	public String prefix = "";
	
	public String id() {
		return "" + ec.getID();
	}
	
	public TRoRRenderer(EmulatedComputer ec, OutputStream output, InputStream input) {
		this.ec = ec;
		this.output = output;
		this.input = new AsyncScanner(input);
		
		if (ec.terminal instanceof TRoRTerminal) {
			term = (TRoRTerminal) ec.terminal;
		} else {
			throw new IllegalArgumentException("Cannot create TRoR renderer unless TRoR terminal is used");
		}
		
		this.input.start();
	}
	
	public TRoRRenderer(EmulatedComputer ec) {
		this(ec, System.out, System.in);
		prefix = "[TRoR]";
	}
	
	@Override
	public boolean isVisible() {
		return !paused;
	}
	
	@Override
	public void setVisible(boolean visible) {
		paused = !visible;
	}
	
	@Override
	public void resize(int width, int height) {}
	
	private boolean send(TRoRPacket<? extends Object> packet) {
		try {
			output.write((prefix + packet.toString(id())).getBytes());
			return true;
		} catch (IOException e) {
			ec.emu.logger.debug("failed to send tror packet", e);
			return false;
		}
	}
	
	@Override
	public void onAdvance(double dt) {
		if (!paused) {
			if (firstsend) {
				firstsend = false;
				send(new CapabilitiesPacket(ImmutableSet.of("ccemux")));
			}
			
			term.popQueue().forEach(p -> send(p));
			
			while (input.hasLines())
				input.getLines().forEach(l -> {
					MatchResult m = matches(l, eventPattern);
					
					if (m != null && m.group(1).equals(id()))
						handlers.forEach((p, f) -> {
							MatchResult m2 = matches(m.group(2), p);
							if (m2 != null)
								f.accept(ec, m2);
						});
				});
		}
	}
	
	@Override
	public void onDispose() {
		send(new ConnectionClosedPacket("Emulator closed"));
	}
	
	@Override
	public void onTerminalResized(int width, int height) {
		send(new ResizePacket(width, height));
	}
}
