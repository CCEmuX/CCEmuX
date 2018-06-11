package net.clgd.ccemux.api.plugins;

import javax.annotation.Nonnull;

import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.peripheral.PeripheralFactory;
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
	@Nonnull
	EmuConfig config();

	/**
	 * Register a new renderer factory
	 *
	 * @param name    The name of this renderer factory
	 * @param factory The factory to register
	 * @throws NullPointerException  If either argument is {@code null}
	 * @throws IllegalStateException If there is already a renderer with the same name.
	 */
	void addRenderer(@Nonnull String name, @Nonnull RendererFactory<?> factory);

	/**
	 * Register a new peripheral factory
	 *
	 * @param name    The name of the factory
	 * @param factory The factory to register
	 * @throws NullPointerException  If either argument is {@code null}
	 * @throws IllegalStateException If there is already a peripheral with the same name.
	 */
	void addPeripheral(@Nonnull String name, @Nonnull PeripheralFactory<?> factory);
}
