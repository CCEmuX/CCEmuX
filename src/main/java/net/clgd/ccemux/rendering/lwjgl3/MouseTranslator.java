package net.clgd.ccemux.rendering.lwjgl3;

import static org.lwjgl.glfw.GLFW.*;

public class MouseTranslator {
	public static int lwjglToCC(int button) {
		switch (button) {
			case GLFW_MOUSE_BUTTON_LEFT:
				return 1;
			case GLFW_MOUSE_BUTTON_RIGHT:
				return 2;
			case GLFW_MOUSE_BUTTON_MIDDLE:
				return 3;
			default:
				return button;
		}
	}
	
	public static int ccToLWJGL(int button) {
		switch (button) {
			case 1:
				return GLFW_MOUSE_BUTTON_LEFT;
			case 2:
				return GLFW_MOUSE_BUTTON_RIGHT;
			case 3:
				return GLFW_MOUSE_BUTTON_MIDDLE;
			default:
				return button;
		}
	}
}
