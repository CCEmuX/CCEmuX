package net.clgd.ccemux.rendering;

import net.clgd.ccemux.emulation.EmulatedComputer;

public interface Renderer extends EmulatedComputer.Listener {
	public static interface Listener {
		public void onClosed();
	}

	public boolean isVisible();

	public void setVisible(boolean visible);

	public void dispose();

	public void resize(int width, int height);

	public void addListener(Listener l);

	public void removeListener(Listener l);
}
