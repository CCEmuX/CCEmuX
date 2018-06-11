package net.clgd.ccemux.api.plugins.hooks;

import javax.annotation.Nonnull;

import net.clgd.ccemux.api.emulation.Emulator;

/**
 * This hook is called every single tick while CCEmuX is emulating the computers
 * - 20 times per second, assuming the (real) computer is fast enough. This hook
 * is not recommended unless you have no other option, because it will have a
 * significant impact on performance.
 *
 * @author apemanzilla
 */
@FunctionalInterface
public interface Tick extends Hook {
	void onTick(@Nonnull Emulator emu, double dt);
}
