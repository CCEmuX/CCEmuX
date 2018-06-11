package net.clgd.ccemux.api.rendering;

import javax.annotation.Nonnull;

import net.clgd.ccemux.api.emulation.EmulatedComputer;

/**
 * A renderer used to draw the terminal and capture input for a computer
 */
public interface Renderer extends EmulatedComputer.Listener {
	/**
	 * A listener invoked when a renderer is closed
	 */
	@FunctionalInterface
	interface Listener {
		void onClosed();
	}

	/**
	 * Whether this renderer is currently visible to users
	 */
	boolean isVisible();

	/**
	 * Sets user visibility of this renderer
	 */
	void setVisible(boolean visible);

	/**
	 * Destroys this renderer
	 */
	void dispose();

	/**
	 * Adds a listener to this renderer
	 */
	void addListener(@Nonnull Listener l);

	/**
	 * Removes a listener from this renderer
	 */
	void removeListener(@Nonnull Listener l);

	@Override
	default void onAdvance(double dt) {}
}
