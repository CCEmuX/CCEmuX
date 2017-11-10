package net.clgd.ccemux.rendering;

import net.clgd.ccemux.emulation.EmulatedComputer;

import java.io.IOException;
import java.net.URL;

public interface Renderer extends EmulatedComputer.Listener {
	interface Listener {
		void onClosed();
	}

	boolean isVisible();

	void setVisible(boolean visible);

	void dispose();

	void addListener(Listener l);

	void removeListener(Listener l);

	TerminalFont loadFont(URL url) throws IOException;
}
