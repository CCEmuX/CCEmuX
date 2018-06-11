package net.clgd.ccemux.api.peripheral;

import javax.annotation.Nonnull;

import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;

/**
 * A factory used to create a peripheral for a given computer
 *
 * @param <T> The type of peripheral created by this factory
 */
@FunctionalInterface
public interface PeripheralFactory<T extends Peripheral> {
	/**
	 * Creates a peripheral for the given computer and config.
	 *
	 * A peripheral may implement {@link EmulatedComputer.Listener} in order to be updated
	 * every tick.
	 */
	@Nonnull
	T create(@Nonnull EmulatedComputer computer, @Nonnull EmuConfig cfg);
}
