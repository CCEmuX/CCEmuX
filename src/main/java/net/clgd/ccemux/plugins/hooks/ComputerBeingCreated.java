package net.clgd.ccemux.plugins.hooks;

import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.Hook;

/**
 * Invoked while an {@link EmulatedComputer} is being created by CCEmuX, but
 * before it is actually constructed. This hook is the last call before the
 * construction of the emulated computer, so the {@link EmulatedComputer.Builder
 * Builder} will not be modified by CCEmuX - but it may be modified by other
 * plugins which also use this hook.
 * 
 * @author apemanzilla
 * @see ComputerCreated
 * @see RendererCreated
 * @see ComputerRemoved
 */
@FunctionalInterface
public interface ComputerBeingCreated extends Hook {
	public void onComputerBeingCreated(CCEmuX emu, EmulatedComputer.Builder builder);
}
