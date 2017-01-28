package net.clgd.ccemux.rendering.lwjgl3;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import net.clgd.ccemux.Utils;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.lwjgl3.KeyTranslator;
import net.clgd.ccemux.rendering.awt.TerminalComponent;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.opengl.GL;

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
	private int terminalTextList = -1;

	private GLTexture fontTexture;

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
	
	private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			computer.pressKey(KeyTranslator.translateToCC(key), action == GLFW_RELEASE);
		}
	};
	
	private final GLFWCharCallback charCallback = new GLFWCharCallback() {
		@Override
		public void invoke(long window, int codepoint) {
			computer.pressChar((char)codepoint);
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
		glfwSetKeyCallback(window, keyCallback);
		glfwSetCharCallback(window, charCallback);

		glfwShowWindow(window);
		visible = true;

		glfwMakeContextCurrent(window);
		GL.createCapabilities();

		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		resize(getWidth(), getHeight());
		fontTexture = new GLTexture(ComputerCraft.class.getResourceAsStream(TerminalComponent.CC_FONT_PATH));
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

	private void glChar(char c, int x, int y, Color colour) {
		if ((int) c == 0)
			return; // nothing to do here

		// TODO: replace with something non-magical.
		int charWidth = 6;
		int charHeight = 9;

		int fontWidth = 96;
		int fontHeight = 144;

		Point charLocation = TerminalComponent.getCharLocation(c, charWidth, charHeight, fontWidth, fontHeight);

		int dx1 = x;
		int dy1 = y;
		int dx2 = x + pixelWidth;
		int dy2 = y + pixelHeight;

		float sx1 = charLocation.x / (float)fontTexture.getWidth();
		float sy1 = charLocation.y / (float)fontTexture.getHeight();
		float sx2 = (charLocation.x + charWidth) / (float)fontTexture.getWidth();
		float sy2 = (charLocation.y + charHeight) / (float)fontTexture.getHeight();

		glColor4ub((byte)colour.getRed(), (byte)colour.getGreen(), (byte)colour.getBlue(), (byte)255);
		glTexCoord2f(sx1, sy1);
		glVertex2f(dx1, dy1);
		glTexCoord2f(sx1, sy2);
		glVertex2f(dx1, dy2);
		glTexCoord2f(sx2, sy1);
		glVertex2f(dx2, dy1);
		glTexCoord2f(sx2, sy1);
		glVertex2f(dx2, dy1);
		glTexCoord2f(sx1, sy2);
		glVertex2f(dx1, dy2);
		glTexCoord2f(sx2, sy2);
		glVertex2f(dx2, dy2);
	}

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
				TextBuffer bgLine = terminal.getBackgroundColourLine(y);

				int height = (y == 0 || y == terminal.getHeight() - 1) ? pixelHeight + margin : pixelHeight;

				for (int x = 0; x < terminal.getWidth(); x++) {
					int width = (x == 0 || x == terminal.getWidth() - 1) ? pixelWidth + margin : pixelWidth;

					Color bgc = Utils.getCCColourFromChar((bgLine == null) ? 'f' : bgLine.charAt(x));

					glColor3ub((byte)bgc.getRed(), (byte)bgc.getGreen(), (byte)bgc.getBlue());
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

					dx += width;
				}

				dx = 0;
				dy += height;
			}
		}

		glEnd();
		glEndList();
	}

	private void buildTextList() {
		if (terminalTextList < 0) {
			terminalTextList = glGenLists(1);
		}

		glNewList(terminalDisplayList, GL_COMPILE);
		glBegin(GL_TRIANGLES);

		synchronized (computer.terminal) {
			Terminal terminal = computer.terminal;

			int dx = 0;
			int dy = 0;

			for (int y = 0; y < terminal.getHeight(); y++) {
				TextBuffer textLine = terminal.getLine(y);
				TextBuffer fgLine = terminal.getTextColourLine(y);

				int height = (y == 0 || y == terminal.getHeight() - 1) ? pixelHeight + margin : pixelHeight;

				for (int x = 0; x < terminal.getWidth(); x++) {
					int width = (x == 0 || x == terminal.getWidth() - 1) ? pixelWidth + margin : pixelWidth;
					char character = (textLine == null) ? ' ' : textLine.charAt(x);

					Color fgc = Utils.getCCColourFromChar((fgLine == null) ? '0' : fgLine.charAt(x));
					glChar(character, x * pixelWidth + margin, y * pixelHeight + margin, fgc);

					dx += width;
				}

				dx = 0;
				dy += height;
			}
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
			buildTextList();
			computer.terminal.clearChanged();
		}

		glClear(GL_COLOR_BUFFER_BIT);

		if (terminalDisplayList >= 0) glCallList(terminalDisplayList);
		if (terminalTextList >= 0) {
			fontTexture.bind(GL_TEXTURE_2D);
			glCallList(terminalTextList);
		}

		glfwSwapBuffers(window);
		glfwPollEvents();
	}

	@Override
	public void onDispose() {
		setVisible(false);
		fontTexture.close();
		if (terminalDisplayList >= 0) glDeleteLists(terminalDisplayList, 1);
		if (terminalTextList >= 0) glDeleteLists(terminalTextList, 1);
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
