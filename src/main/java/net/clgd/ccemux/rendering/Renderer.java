package net.clgd.ccemux.rendering;

import net.clgd.ccemux.emulation.EmulatedComputer;

public interface Renderer extends EmulatedComputer.Listener {
	public boolean isVisible();
	public void setVisible(boolean visible);
	public void resize(int width, int height);
}
