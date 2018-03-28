package net.clgd.ccemux.api.emulation;

import java.util.ArrayList;
import java.util.List;

import dan200.computercraft.shared.util.Palette;
import lombok.Getter;
import lombok.Setter;

/**
 * A wrapper for {@link Palette} that allows {@link ColorChangeListener
 * listeners} to be added
 *
 */
public class EmulatedPalette extends Palette {
	/**
	 * A listener that's triggered when a color on the palette is changed
	 */
	@FunctionalInterface
	public interface ColorChangeListener {
		void setColour(int index, double r, double g, double b);
	}

	private final Palette delegate;

	private final List<ColorChangeListener> listeners = new ArrayList<>();

	/**
	 * Whether a color in this palette has been changed
	 */
	@Getter
	@Setter
	private boolean changed = true;

	public EmulatedPalette(Palette delegate) {
		this.delegate = delegate;
	}

	/**
	 * Change a color
	 * 
	 * @param i
	 *            The index of the color
	 * @param r
	 *            The red component of the color
	 * @param g
	 *            The green component of the color
	 * @param b
	 *            The blue component of the color
	 */
	@Override
	public void setColour(int i, double r, double g, double b) {
		// The delegate is null when initially creating the object, which means
		// resetColours causes an NPE. Hence this null check.
		if (delegate == null)
			return;

		delegate.setColour(i, r, g, b);
		setChanged(true);

		for (ColorChangeListener listener : listeners)
			listener.setColour(i, r, g, b);
	}

	/**
	 * Resets a color to its default value
	 */
	@Override
	public void resetColour(int i) {
		if (delegate == null)
			return;

		delegate.resetColour(i);
		setChanged(true);
	}

	/**
	 * Resets all colors to their default values
	 */
	@Override
	public void resetColours() {
		if (delegate == null)
			return;

		delegate.resetColours();
		setChanged(true);
	}

	/**
	 * Gets a color's current value
	 */
	@Override
	public double[] getColour(int colour) {
		return delegate.getColour(colour);
	}

	/**
	 * Adds a listener that will be invoked when a color is changed
	 */
	public void addListener(ColorChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a listener
	 */
	public void removeListener(ColorChangeListener listener) {
		listeners.remove(listener);
	}
}
