package net.clgd.ccemux.api.emulation;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import dan200.computercraft.core.terminal.Terminal;

/**
 * A wrapper for {@link Terminal} that allows {@link Listener listeners} to be
 * added
 */
public class EmulatedTerminal extends Terminal {

	public interface Listener {
		default void resize(int width, int height) {}

		default void setCursorPos(int x, int y) {}

		default void setCursorBlink(boolean blink) {}

		default void setTextColour(int colour) {}

		default void setBackgroundColour(int colour) {}

		default void blit(@Nonnull ByteBuffer text, @Nonnull ByteBuffer textColour, @Nonnull ByteBuffer backgroundColour) {
			blit(LuaHelpers.decode(text), LuaHelpers.decode(textColour), LuaHelpers.decode(backgroundColour));
		}

		default void blit(@Nonnull String text, @Nonnull String textColour, @Nonnull String backgroundColour) {}

		default void write(@Nonnull String text) {}

		default void scroll(int yDiff) {}

		default void clear() {}

		default void clearLine() {}
	}

	private final EmulatedPalette palette;
	private final List<Listener> listeners = new ArrayList<>();
	private final AtomicBoolean changed;
	private int scale;

	public EmulatedTerminal(int width, int height) {
		this(width, height, new AtomicBoolean(false));
	}

	private EmulatedTerminal(int width, int height, AtomicBoolean changed) {
		super(width, height, true, () -> changed.set(true));
		this.palette = new EmulatedPalette(super.getPalette());
		this.changed = changed;
	}

	public boolean getAndClearChanged() {
		return changed.getAndSet(false);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		for (Listener listener : listeners) {
			listener.resize(width, height);
		}
	}

	@Override
	public void setCursorPos(int x, int y) {
		super.setCursorPos(x, y);
		for (Listener listener : listeners) {
			listener.setCursorPos(x, y);
		}
	}

	@Override
	public void setCursorBlink(boolean blink) {
		super.setCursorBlink(blink);
		for (Listener listener : listeners) {
			listener.setCursorBlink(blink);
		}
	}

	@Override
	public void setTextColour(int colour) {
		super.setTextColour(colour);
		for (Listener listener : listeners) {
			listener.setTextColour(colour);
		}
	}

	@Override
	public void setBackgroundColour(int colour) {
		super.setBackgroundColour(colour);
		for (Listener listener : listeners) {
			listener.setBackgroundColour(colour);
		}
	}

	@Override
	public void blit(@Nonnull ByteBuffer text, @Nonnull ByteBuffer textColour, @Nonnull ByteBuffer backgroundColour) {
		super.blit(text, textColour, backgroundColour);
		for (Listener listener : listeners) {
			listener.blit(text, textColour, backgroundColour);
		}
	}

	@Override
	public void write(@Nonnull String text) {
		super.write(text);
		for (Listener listener : listeners) {
			listener.write(text);
		}
	}

	@Override
	public void scroll(int yDiff) {
		super.scroll(yDiff);
		for (Listener listener : listeners) {
			listener.scroll(yDiff);
		}
	}

	@Override
	public void clear() {
		super.clear();
		for (Listener listener : listeners) {
			listener.clear();
		}
	}

	@Override
	public void clearLine() {
		super.clearLine();
		for (Listener listener : listeners) {
			listener.clearLine();
		}
	}

	@Override
	@Nonnull
	public EmulatedPalette getPalette() {
		return palette;
	}

	public void addListener(@Nonnull Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(@Nonnull Listener listener) {
		listeners.remove(listener);
	}
}
