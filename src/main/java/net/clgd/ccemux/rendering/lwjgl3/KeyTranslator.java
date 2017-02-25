package net.clgd.ccemux.rendering.lwjgl3;

import com.google.common.collect.ImmutableBiMap;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class KeyTranslator {
	private static final ImmutableBiMap<Integer, Integer> lwjglToCCMap = ImmutableBiMap.copyOf(new HashMap<Integer, Integer>() {{
		put(GLFW_KEY_1, 2);
		put(GLFW_KEY_2, 3);
		put(GLFW_KEY_3, 4);
		put(GLFW_KEY_4, 5);
		put(GLFW_KEY_5, 6);
		put(GLFW_KEY_6, 7);
		put(GLFW_KEY_7, 8);
		put(GLFW_KEY_8, 9);
		put(GLFW_KEY_9, 10);
		put(GLFW_KEY_0, 11);
		put(GLFW_KEY_MINUS, 12);
		put(GLFW_KEY_EQUAL, 13);
		put(GLFW_KEY_BACKSPACE, 14);
		put(GLFW_KEY_TAB, 15);
		put(GLFW_KEY_Q, 16);
		put(GLFW_KEY_W, 17);
		put(GLFW_KEY_E, 18);
		put(GLFW_KEY_R, 19);
		put(GLFW_KEY_T, 20);
		put(GLFW_KEY_Y, 21);
		put(GLFW_KEY_U, 22);
		put(GLFW_KEY_I, 23);
		put(GLFW_KEY_O, 24);
		put(GLFW_KEY_P, 25);
		put(GLFW_KEY_LEFT_BRACKET, 26);
		put(GLFW_KEY_RIGHT_BRACKET, 27);
		put(GLFW_KEY_ENTER, 28);
		put(GLFW_KEY_LEFT_CONTROL, 29);
		put(GLFW_KEY_A, 30);
		put(GLFW_KEY_S, 31);
		put(GLFW_KEY_D, 32);
		put(GLFW_KEY_F, 33);
		put(GLFW_KEY_G, 34);
		put(GLFW_KEY_H, 35);
		put(GLFW_KEY_J, 36);
		put(GLFW_KEY_K, 37);
		put(GLFW_KEY_L, 38);
		put(GLFW_KEY_SEMICOLON, 39);
		put(GLFW_KEY_APOSTROPHE, 40);
		put(GLFW_KEY_GRAVE_ACCENT, 41);
		put(GLFW_KEY_LEFT_SHIFT, 42);
		put(GLFW_KEY_BACKSLASH, 43);
		put(GLFW_KEY_Z, 44);
		put(GLFW_KEY_X, 45);
		put(GLFW_KEY_C, 46);
		put(GLFW_KEY_V, 47);
		put(GLFW_KEY_B, 48);
		put(GLFW_KEY_N, 49);
		put(GLFW_KEY_M, 50);
		put(GLFW_KEY_COMMA, 51);
		put(GLFW_KEY_PERIOD, 52);
		put(GLFW_KEY_SLASH, 53);
		put(GLFW_KEY_RIGHT_SHIFT, 54);
		put(GLFW_KEY_KP_MULTIPLY, 55);
		put(GLFW_KEY_LEFT_ALT, 56);
		put(GLFW_KEY_SPACE, 57);
		put(GLFW_KEY_CAPS_LOCK, 58);
		put(GLFW_KEY_F1, 59);
		put(GLFW_KEY_F2, 60);
		put(GLFW_KEY_F3, 61);
		put(GLFW_KEY_F4, 62);
		put(GLFW_KEY_F5, 63);
		put(GLFW_KEY_F6, 64);
		put(GLFW_KEY_F7, 65);
		put(GLFW_KEY_F8, 66);
		put(GLFW_KEY_F9, 67);
		put(GLFW_KEY_F10, 68);
		put(GLFW_KEY_NUM_LOCK, 69);
		put(GLFW_KEY_SCROLL_LOCK, 70);
		put(GLFW_KEY_KP_7, 71);
		put(GLFW_KEY_KP_8, 72);
		put(GLFW_KEY_KP_9, 73);
		put(GLFW_KEY_KP_SUBTRACT, 74);
		put(GLFW_KEY_KP_4, 75);
		put(GLFW_KEY_KP_5, 76);
		put(GLFW_KEY_KP_6, 77);
		put(GLFW_KEY_KP_ADD, 78);
		put(GLFW_KEY_KP_1, 79);
		put(GLFW_KEY_KP_2, 80);
		put(GLFW_KEY_KP_3, 81);
		put(GLFW_KEY_KP_DECIMAL, 83);
		put(GLFW_KEY_F11, 87);
		put(GLFW_KEY_F12, 88);
		put(GLFW_KEY_F13, 100);
		put(GLFW_KEY_F14, 101);
		put(GLFW_KEY_F15, 102);
		put(GLFW_KEY_KP_EQUAL, 141);
		put(GLFW_KEY_KP_ENTER, 156);
		put(GLFW_KEY_RIGHT_CONTROL, 157);
		put(GLFW_KEY_KP_DIVIDE, 181);
		put(GLFW_KEY_RIGHT_ALT, 184);
		put(GLFW_KEY_PAUSE, 197);
		put(GLFW_KEY_HOME, 199);
		put(GLFW_KEY_UP, 200);
		put(GLFW_KEY_PAGE_UP, 201);
		put(GLFW_KEY_LEFT, 203);
		put(GLFW_KEY_RIGHT, 205);
		put(GLFW_KEY_END, 207);
		put(GLFW_KEY_DOWN, 208);
		put(GLFW_KEY_PAGE_DOWN, 209);
		put(GLFW_KEY_INSERT, 210);
		put(GLFW_KEY_DELETE, 211);
	}});
	
	public static int translateToCC(int keycode) {
		return lwjglToCCMap.getOrDefault(keycode, 0);
	}
	
	public static int translateToSwing(int keycode) {
		return lwjglToCCMap.inverse().getOrDefault(keycode, 0);
	}
}
