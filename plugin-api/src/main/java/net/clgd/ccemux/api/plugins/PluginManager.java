package net.clgd.ccemux.api.plugins;

import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.rendering.RendererFactory;

/**
 * Used when initialising plugins in order to register various providers.
 *
 * @see Plugin#setup(PluginManager)
 */
public interface PluginManager {
	/**
	 * The configuration for the emulator.
	 *
	 * @return This emulator's config options.
	 */
	EmuConfig config();

	/**
	 * Register a new renderer factory
	 *
	 * @param name    The name of this renderer factory
	 * @param factory The factory to register
	 * @throws NullPointerException  If either argument is {@code null}
	 * @throws IllegalStateException If there is already a renderer with the same name.
	 */
	void addRenderer(String name, RendererFactory<?> factory);
}
