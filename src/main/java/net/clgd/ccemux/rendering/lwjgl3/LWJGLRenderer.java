package net.clgd.ccemux.rendering.lwjgl3;

import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import net.clgd.ccemux.Utils;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.awt.TerminalComponent;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.opengl.GL11.*;

public class LWJGLRenderer implements Renderer {
	private long window = NULL;

	public static final String EMU_WINDOW_TITLE = "CCEmuX";

	public final int pixelWidth;
	public final int pixelHeight;

	public final EmulatedComputer computer;

	private boolean visible = false;

	public final int margin = 2;

	private int terminalDisplayList = -1;

	private final GLFWWindowCloseCallback closeCallback = new GLFWWindowCloseCallback() {
		@Override
		public void invoke(long window) {
			glfwDestroyWindow(window);
			computer.dispose();
			LWJGLRenderer.this.window = NULL;
		}
	};

	private final GLFWErrorCallback errorCallback = new GLFWErrorCallback() {
		@Override
		public void invoke(int error, long description) {
			computer.emu.logger.error("GLFW Error ({}): {}", error, memDecodeUTF8(description));
		}
	};

	public LWJGLRenderer(EmulatedComputer computer) {
		glfwInit();

		this.computer = computer;

		pixelWidth = (int)(6.0f * computer.emu.conf.getTermScale());
		pixelHeight = (int)(9.0f * computer.emu.conf.getTermScale());

		glfwSetErrorCallback(errorCallback);

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_RESIZABLE, 0);
		glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);

		window = glfwCreateWindow(
			getWidth(), getHeight(),
			"CCEmuX (LWJGL 3)",
			NULL, NULL
		);

		if (window == NULL) {
			computer.emu.logger.error("glfwCreateWindow failed!!");
			return;
		}

		glfwSetWindowCloseCallback(window, closeCallback);

		glfwShowWindow(window);
		visible = true;

		glfwMakeContextCurrent(window);
		GL.createCapabilities();

		resize(getWidth(), getHeight());
	}

	public int getWidth() {
		return pixelWidth * computer.terminal.getWidth() + margin * 2;
	}

	public int getHeight() {
		return pixelHeight * computer.terminal.getHeight() + margin * 2;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			glfwShowWindow(window);
		} else {
			glfwHideWindow(window);
		}

		this.visible = visible;
	}

	@Override
	public void resize(int width, int height) {
		glfwSetWindowSize(window, width, height);

		glViewport(0, 0, width, height);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0.0, getWidth(), getHeight(), 0.0, -1.0, 1.0);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}

/*	private void glChar(Graphics g, char c, int x, int y, int color) {
		if ((int) c == 0)
			return; // nothing to do here

		// TODO: replace with something non-magical.
		int charWidth = 6;
		int charHeight = 9;

		int fontWidth = 96;
		int fontHeight = 144;

		Point charLocation = TerminalComponent.getCharLocation(c, charWidth, charHeight, fontWidth, fontHeight);

		g.drawImage(
				// tinted char
				fontImages[color],

				// destination
				x, y, x + pixelWidth, y + pixelHeight,

				// source
				charLocation.x, charLocation.y, charLocation.x + charWidth, charLocation.y + charHeight,

				null);
	}*/

	private void buildTerminalList() {
		if (terminalDisplayList < 0) {
			terminalDisplayList = glGenLists(1);
		}

		final Random rand = new Random();

		glNewList(terminalDisplayList, GL_COMPILE);
		glBegin(GL_TRIANGLES);

		synchronized (computer.terminal) {
			Terminal terminal = computer.terminal;

			int dx = 0;
			int dy = 0;

			for (int y = 0; y < terminal.getHeight(); y++) {
				TextBuffer textLine = terminal.getLine(y);
				TextBuffer bgLine = terminal.getBackgroundColourLine(y);
				TextBuffer fgLine = terminal.getTextColourLine(y);

				int height = (y == 0 || y == terminal.getHeight() - 1) ? pixelHeight + margin : pixelHeight;

				for (int x = 0; x < terminal.getWidth(); x++) {
					int width = (x == 0 || x == terminal.getWidth() - 1) ? pixelWidth + margin : pixelWidth;

					Color bgc = Utils.getCCColourFromChar((bgLine == null) ? 'f' : bgLine.charAt(x));

					//glColor3ub((byte)bgc.getRed(), (byte)bgc.getGreen(), (byte)bgc.getBlue());
					glColor3ub((byte)rand.nextInt(255), (byte)rand.nextInt(255), (byte)rand.nextInt(255));
					glTexCoord2f(0.0f, 0.0f);
					glVertex2f(dx, dy);
					glTexCoord2f(0.0f, 1.0f);
					glVertex2f(dx, dy + height);
					glTexCoord2f(1.0f, 0.0f);
					glVertex2f(dx + width, dy);
					glTexCoord2f(1.0f, 0.0f);
					glVertex2f(dx + width, dy);
					glTexCoord2f(0.0f, 1.0f);
					glVertex2f(dx, dy + height);
					glTexCoord2f(1.0f, 1.0f);
					glVertex2f(dx + width, dy + height);

					/*char character = (textLine == null) ? ' ' : textLine.charAt(x);
					char fgChar = (fgLine == null) ? ' ' : fgLine.charAt(x);*/

					/*drawChar(g, character, x * pixelWidth + margin, y * pixelHeight + margin,
							Utils.base16ToInt(fgChar));*/

					dx += width;
				}

				dx = 0;
				dy += height;
			}

			/*boolean blink = terminal.getCursorBlink() && (blinkLocked || CCEmuX.getGlobalCursorBlink());

			if (blink) {
				drawChar(g, cursorChar, terminal.getCursorX() * pixelWidth + margin,
						terminal.getCursorY() * pixelHeight + margin, terminal.getTextColour());
			}

			g.dispose();*/
		}

		glEnd();
		glEndList();
	}

	@Override
	public void onAdvance(double dt) {
		if (window == NULL) {
			return;
		}

		glfwSetWindowTitle(window, getWindowTitle());

		if (computer.terminal.getChanged()) {
			buildTerminalList();
			computer.terminal.clearChanged();
		}

		glClear(GL_COLOR_BUFFER_BIT);

		if (terminalDisplayList >= 0) {
			glCallList(terminalDisplayList);
		}

		glfwSwapBuffers(window);
		glfwPollEvents();
	}

	@Override
	public void onDispose() {
		setVisible(false);
		glDeleteLists(terminalDisplayList, 1);

		if (window != NULL) {
			glfwDestroyWindow(window);
		}

		glfwTerminate();
	}

	@Override
	public void onTerminalResized(int width, int height) {
		resize(
			pixelWidth * computer.terminal.getWidth() + margin * 2,
			pixelHeight * computer.terminal.getHeight() + margin * 2
		);
	}

	private String getWindowTitle() {
		int id = computer.getID();
		String title = EMU_WINDOW_TITLE + " - ";

		if (computer.getLabel() != null) {
			title += computer.getLabel() + " (Computer #" + id + ")";
		} else {
			title += "Computer #" + id;
		}

		return title;
	}
}
