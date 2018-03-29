package net.clgd.ccemux.plugins.hooks;

import net.clgd.ccemux.api.emulation.Emulator;

/**
 * This hook is called when CCEmuX is closing, after all computers have been
 * removed and emulation has stopped. It can be used to free resources.
 * 
 * @author apemanzilla
 * @see InitializationCompleted
 */
@FunctionalInterface
public interface Closing extends Hook {
	public void onClosing(Emulator emu);
}
