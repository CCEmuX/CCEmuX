package net.clgd.ccemux.rendering.lwjgl3;

import net.clgd.ccemux.Launcher;
import net.clgd.ccemux.Runner;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.Renderer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class LWJGLRenderer implements Renderer {
	private long window = NULL;

	public static final String EMU_WINDOW_TITLE = "CCEmuX";

	public final int pixelWidth;
	public final int pixelHeight;

	public final EmulatedComputer computer;

	private boolean visible = false;

	public final int margin = 2;

	public LWJGLRenderer(EmulatedComputer computer) {
		glfwInit();

		this.computer = computer;

		pixelWidth = 6 * computer.emu.conf.getTermScale();
		pixelHeight = 9 * computer.emu.conf.getTermScale();

		glfwSetErrorCallback(new GLFWErrorCallback() {
			@Override
			public void invoke(int error, long description) {
				computer.emu.logger.error("GLFW Error ({}): {}", error, memDecodeUTF8(description));
			}
		});

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_RESIZABLE, 0);
		glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);

		window = glfwCreateWindow(
			pixelWidth * computer.terminal.getWidth() + margin * 2,
			pixelHeight * computer.terminal.getHeight() + margin * 2,
			"CCEmuX (LWJGL 3)",
			NULL, NULL
		);

		if (window == NULL) {

			return;
		}

		glfwSetWindowCloseCallback(window, new GLFWWindowCloseCallback() {
			@Override
			public void invoke(long window) {
				glfwDestroyWindow(window);
				LWJGLRenderer.this.window = NULL;
			}
		});

		glfwShowWindow(window);
		visible = true;

		glfwMakeContextCurrent(window);
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
	}

	@Override
	public void onAdvance(double dt) {
		if (window != NULL) {
			glfwSetWindowTitle(window, getWindowTitle());
		}

		glfwPollEvents();
	}

	@Override
	public void onDispose() {
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
