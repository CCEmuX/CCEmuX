package net.clgd.ccemux.api.plugins.hooks;

import javax.annotation.Nonnull;

import net.clgd.ccemux.api.emulation.Emulator;
import net.clgd.ccemux.api.rendering.Renderer;

/**
 * Called immediately after a {@link Renderer} is created.
 *
 * @author apemanzilla
 * @see CreatingComputer
 * @see ComputerCreated
 * @see ComputerRemoved
 */
@FunctionalInterface
public interface RendererCreated extends Hook {
	void onRendererCreated(@Nonnull Emulator emu, @Nonnull Renderer renderer);
}
