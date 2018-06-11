package net.clgd.ccemux.api.peripheral;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.emulation.EmulatedComputer;

public interface Peripheral extends IPeripheral, EmulatedComputer.Listener {
	/**
	 * Setup any configuration options this peripheral requires.
	 *
	 * For instance, modems may wish to provide configuration options for their
	 * position and range.
	 *
	 * @param group The group to load config elements from.
	 */
	default void configSetup(@Nonnull Group group) { }

	/**
	 * Called when the owning computer is ticked in order to perform any processing
	 * which must be done on the main thread.
	 *
	 * @param dt The change in time in seconds since the last update. This will normally be
	 *           around {@code 0.05}.
	 */
	@Override
	default void onAdvance(double dt) { }

	@Override
	default boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}
}
