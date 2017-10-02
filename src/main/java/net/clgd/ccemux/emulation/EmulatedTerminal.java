package net.clgd.ccemux.emulation;

import java.util.ArrayList;
import java.util.List;

import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.util.Palette;

public class EmulatedTerminal extends Terminal {

	public interface Listener {
		default void resize(int width, int height) {}

		default void setCursorPos(int x, int y) {}

		default void setCursorBlink(boolean blink) {}

		default void setTextColour(int colour) {}

		default void setBackgroundColour(int colour) {}

		default void blit(String text, String textColour, String backgroundColour) {}

		default void write(String text) {}

		default void scroll(int yDiff) {}

		default void clear() {}

		default void clearLine() {}
	}

	private final EmulatedPalette palette;
	private final List<Listener> listeners = new ArrayList<>();

	public EmulatedTerminal(int width, int height) {
		super(width, height);
		this.palette = new EmulatedPalette(super.getPalette());
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		for (Listener listener : listeners)
			listener.resize(width, height);
	}

	@Override
	public void setCursorPos(int x, int y) {
		super.setCursorPos(x, y);
		for (Listener listener : listeners)
			listener.setCursorPos(x, y);
	}

	@Override
	public void setCursorBlink(boolean blink) {
		super.setCursorBlink(blink);
		for (Listener listener : listeners)
			listener.setCursorBlink(blink);
	}

	@Override
	public void setTextColour(int colour) {
		super.setTextColour(colour);
		for (Listener listener : listeners)
			listener.setTextColour(colour);
	}

	@Override
	public void setBackgroundColour(int colour) {
		super.setBackgroundColour(colour);
		for (Listener listener : listeners)
			listener.setBackgroundColour(colour);
	}

	@Override
	public void blit(String text, String textColour, String backgroundColour) {
		super.blit(text, textColour, backgroundColour);
		for (Listener listener : listeners)
			listener.blit(text, textColour, backgroundColour);
	}

	@Override
	public void write(String text) {
		super.write(text);
		for (Listener listener : listeners)
			listener.write(text);
	}

	@Override
	public void scroll(int yDiff) {
		super.scroll(yDiff);
		for (Listener listener : listeners)
			listener.scroll(yDiff);
	}

	@Override
	public void clear() {
		super.clear();
		for (Listener listener : listeners)
			listener.clear();
	}

	@Override
	public void clearLine() {
		super.clearLine();
		for (Listener listener : listeners)
			listener.clearLine();
	}

	public EmulatedPalette getEmulatedPalette() {
		return palette;
	}

	@Override
	public Palette getPalette() {
		return palette;
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
}
