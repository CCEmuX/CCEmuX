package net.clgd.ccemux.rendering.awt;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;

import dan200.computercraft.core.terminal.Terminal;

class TerminalComponent extends Canvas {
	private static final long serialVersionUID = -5043543826280613143L;

	private final Terminal terminal;
	private final TerminalRenderer renderer;

	boolean blinkLocked = false;

	public TerminalComponent(Terminal terminal, double termScale) {
		this.terminal = terminal;
		this.renderer = new TerminalRenderer(terminal, termScale);
		resizeTerminal();
	}

	public int getMargin() {
		return this.renderer.getMargin();
	}

	void resizeTerminal() {
		Dimension termDimensions = renderer.getSize();

		setSize(termDimensions);
		setPreferredSize(termDimensions);
	}

	private void renderTerminal(AWTTerminalFont font) {
		synchronized (terminal) {
			Graphics g = getBufferStrategy().getDrawGraphics();
			renderer.render(font, g);
			g.dispose();
		}
	}

	public void render(AWTTerminalFont font) {
		if (getBufferStrategy() == null) {
			createBufferStrategy(2);
		}

		do {
			do {
				renderTerminal(font);
			} while (getBufferStrategy().contentsRestored());

			getBufferStrategy().show();
		} while (getBufferStrategy().contentsLost());
	}
}
