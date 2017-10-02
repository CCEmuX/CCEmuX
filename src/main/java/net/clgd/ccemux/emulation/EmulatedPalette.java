package net.clgd.ccemux.emulation;

import java.util.ArrayList;
import java.util.List;

import dan200.computercraft.shared.util.Palette;

public class EmulatedPalette extends Palette {
	@FunctionalInterface
	public interface Listener {
		void setColour(int index, double r, double g, double b);
	}

	private final Palette delegate;

	private final List<Listener> listeners = new ArrayList<>();

	public EmulatedPalette(Palette delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setColour(int i, double r, double g, double b) {
		// The delegate is null when initially creating the object, which means resetColours
		// causes an NPE. Hence this null check.
		if (delegate == null) return;

		delegate.setColour(i, r, g, b);
		for (Listener listener : listeners) listener.setColour(i, r, g, b);
	}

	@Override
	public double[] getColour(int colour) {
		return delegate.getColour(colour);
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
}
