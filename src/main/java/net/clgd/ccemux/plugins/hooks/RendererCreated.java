package net.clgd.ccemux.plugins.hooks;

import net.clgd.ccemux.api.rendering.Renderer;
import net.clgd.ccemux.emulation.CCEmuX;

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
	public void onRendererCreated(CCEmuX emu, Renderer renderer);
}
