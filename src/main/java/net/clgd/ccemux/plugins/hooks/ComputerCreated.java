package net.clgd.ccemux.plugins.hooks;

import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;

/**
 * Invoked immediately after an {@link EmulatedComputer} is created by CCEmuX,
 * but before it is started or emulated. This hook is invoked before any
 * {@link Renderer} instances are created. This hook can be used to add Lua
 * APIs, for example.
 * 
 * @author apemanzilla
 * @see CreatingComputer
 * @see RendererCreated
 * @see ComputerRemoved
 */
@FunctionalInterface
public interface ComputerCreated extends Hook {
	public void onComputerCreated(CCEmuX emu, EmulatedComputer computer);
}
