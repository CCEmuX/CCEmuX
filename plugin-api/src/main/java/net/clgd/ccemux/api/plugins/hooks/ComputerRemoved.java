package net.clgd.ccemux.api.plugins.hooks;

import javax.annotation.Nonnull;

import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.emulation.Emulator;

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
	void onComputerRemoved(@Nonnull Emulator emu, @Nonnull EmulatedComputer computer);
}
