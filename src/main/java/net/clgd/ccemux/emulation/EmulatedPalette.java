package net.clgd.ccemux.emulation;

import java.util.ArrayList;
import java.util.List;

import dan200.computercraft.shared.util.Palette;
import lombok.Getter;

public class EmulatedPalette extends Palette {
	@FunctionalInterface
	public interface ColorChangeListener {
		void setColour(int index, double r, double g, double b);
	}

	private final Palette delegate;

	private final List<ColorChangeListener> listeners = new ArrayList<>();

	@Getter
	private boolean changed = true;

	public EmulatedPalette(Palette delegate) {
		this.delegate = delegate;
	}

	public void setChanged() {
		changed = true;
	}

	public void clearChanged() {
		changed = false;
	}

	@Override
	public void setColour(int i, double r, double g, double b) {
		// The delegate is null when initially creating the object, which means
		// resetColours causes an NPE. Hence this null check.
		if (delegate == null)
			return;

		delegate.setColour(i, r, g, b);
		setChanged();
		for (ColorChangeListener listener : listeners)
			listener.setColour(i, r, g, b);
	}

	@Override
	public void resetColour(int i) {
		if (delegate == null)
			return;

		delegate.resetColour(i);
		setChanged();
	}

	@Override
	public void resetColours() {
		if (delegate == null)
			return;

		delegate.resetColours();
		setChanged();
	}

	@Override
	public double[] getColour(int colour) {
		return delegate.getColour(colour);
	}

	public void addListener(ColorChangeListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ColorChangeListener listener) {
		listeners.remove(listener);
	}
}
