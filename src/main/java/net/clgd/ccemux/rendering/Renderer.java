package net.clgd.ccemux.rendering;

import net.clgd.ccemux.emulation.EmulatedComputer;

public interface Renderer extends EmulatedComputer.Listener {
	boolean isVisible();
	void setVisible(boolean visible);
	void resize(int width, int height);
}
