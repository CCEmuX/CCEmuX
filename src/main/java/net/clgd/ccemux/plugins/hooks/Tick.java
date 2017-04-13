package net.clgd.ccemux.plugins.hooks;

import net.clgd.ccemux.emulation.CCEmuX;

/**
 * This hook is called every single tick while CCEmuX is emulating the computers
 * - 20 times per second, assuming the (real) computer is fast enough.
 * 
 * @author apemanzilla
 *
 */
@FunctionalInterface
public interface Tick extends Hook {
	public void onTick(CCEmuX emu, double dt);
}
