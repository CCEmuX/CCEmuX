package net.clgd.ccemux.emulation.tror;

import dan200.computercraft.core.terminal.Terminal;
import java.util.LinkedList;
import net.clgd.ccemux.emulation.CCEmuX;

import static net.clgd.ccemux.Utils.BASE_16;

public class TRoRTerminal extends Terminal {
	public final LinkedList<TRoRPacket<? extends Object>> trorQueue = new LinkedList<>();

	public final CCEmuX emu;

	public TRoRTerminal(CCEmuX emu, int termWidth, int termHeight) {
		super(termWidth, termHeight);
		this.emu = emu;
	}

	protected void queue(TRoRPacket<? extends Object> packet) {
		synchronized (trorQueue) {
			trorQueue.add(packet);
		}
	}

	public LinkedList<TRoRPacket<? extends Object>> popQueue() {
		synchronized (trorQueue) {
			LinkedList<TRoRPacket<? extends Object>> out = new LinkedList<>(trorQueue);
			trorQueue.clear();
			return out;
		}
	}

	@Override
	public void resize(int w, int h) {
		super.resize(w, h);
		queue(new ResizePacket(w, h));
	}

	@Override
	public void setCursorPos(int x, int y) {
		super.setCursorPos(x, y);
		queue(new CursorPosPacket(x, y));
	}

	@Override
	public void setCursorBlink(boolean blink) {
		super.setCursorBlink(blink);
		queue(new CursorBlinkPacket(blink));
	}

	@Override
	public void setTextColour(int c) {
		super.setTextColour(c);
		queue(new TextColorPacket(BASE_16.charAt(c)));
	}

	@Override
	public void setBackgroundColour(int c) {
		super.setBackgroundColour(c);
		queue(new BackgroundColorPacket(BASE_16.charAt(c)));
	}

	@Override
	public void blit(String text, String fg, String bg) {
		super.blit(text, fg, bg);
		int y = getCursorY();
		queue(
			new BlitLinePacket(getLine(y).toString(), getTextColourLine(y).toString(), getBackgroundColourLine(y).toString()));
	}

	@Override
	public void write(String text) {
		super.write(text);
		queue(new WritePacket(text));
	}

	@Override
	public void scroll(int lines) {
		super.scroll(lines);
		queue(new ScrollPacket(lines));
	}

	@Override
	public void clear() {
		super.clear();
		queue(new ClearPacket());
	}

	@Override
	public void clearLine() {
		super.clearLine();
		queue(new ClearLinePacket());
	}

	@Override
	public void setLine(int line, String text, String fg, String bg) {
		super.setLine(line, text, fg, bg);
		queue(new CursorPosPacket(1, line));
		queue(new BlitLinePacket(text, fg, bg));
	}
}
