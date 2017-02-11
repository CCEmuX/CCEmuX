package net.clgd.ccemux.plugins.hooks;

import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.plugins.Hook;
import net.clgd.ccemux.rendering.Renderer;

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
