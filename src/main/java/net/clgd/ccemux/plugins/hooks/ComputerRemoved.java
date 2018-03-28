package net.clgd.ccemux.plugins.hooks;

import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.emulation.CCEmuX;

/**
 * This hook is called after a computer has been removed from emulation.
 * 
 * @author apemanzilla
 * @see CreatingComputer
 * @see ComputerCreated
 * @see RendererCreated
 */
@FunctionalInterface
public interface ComputerRemoved extends Hook {
	public void onComputerRemoved(CCEmuX emu, EmulatedComputer computer);
}
