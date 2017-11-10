package net.clgd.ccemux.rendering;

import java.io.IOException;
import java.net.URL;

import net.clgd.ccemux.emulation.EmulatedComputer;

public abstract class Renderer implements EmulatedComputer.Listener {
	public interface Listener {
		void onClosed();
	}

	public abstract boolean isVisible();

	public abstract void setVisible(boolean visible);

	public abstract void dispose();

	public abstract void addListener(Listener l);

	public abstract void removeListener(Listener l);

	public abstract TerminalFont loadFont(URL url) throws IOException;
}
