package net.clgd.ccemux.emulation;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import com.google.common.collect.ImmutableBiMap;

// written in Java to improve performance
public class KeyTranslator {
	@SuppressWarnings("serial")
	private static final ImmutableBiMap<Integer, Integer> swingToCCMap = ImmutableBiMap.copyOf(new HashMap<Integer, Integer>() {{
		put(KeyEvent.VK_1, 2);
		put(KeyEvent.VK_2, 3);
		put(KeyEvent.VK_3, 4);
		put(KeyEvent.VK_4, 5);
		put(KeyEvent.VK_5, 6);
		put(KeyEvent.VK_6, 7);
		put(KeyEvent.VK_7, 8);
		put(KeyEvent.VK_8, 9);
		put(KeyEvent.VK_9, 10);
		put(KeyEvent.VK_0, 11);
		put(KeyEvent.VK_MINUS, 12);
		put(KeyEvent.VK_EQUALS, 13);
		put(KeyEvent.VK_BACK_SPACE, 14);
		put(KeyEvent.VK_TAB, 15);
		put(KeyEvent.VK_Q, 16);
		put(KeyEvent.VK_W, 17);
		put(KeyEvent.VK_E, 18);
		put(KeyEvent.VK_R, 19);
		put(KeyEvent.VK_T, 20);
		put(KeyEvent.VK_Y, 21);
		put(KeyEvent.VK_U, 22);
		put(KeyEvent.VK_I, 23);
		put(KeyEvent.VK_O, 24);
		put(KeyEvent.VK_P, 25);
		put(KeyEvent.VK_OPEN_BRACKET, 26);
		put(KeyEvent.VK_CLOSE_BRACKET, 27);
		put(KeyEvent.VK_ENTER, 28);
		put(KeyEvent.VK_CONTROL, 29);
		put(KeyEvent.VK_A, 30);
		put(KeyEvent.VK_S, 31);
		put(KeyEvent.VK_D, 32);
		put(KeyEvent.VK_F, 33);
		put(KeyEvent.VK_G, 34);
		put(KeyEvent.VK_H, 35);
		put(KeyEvent.VK_J, 36);
		put(KeyEvent.VK_K, 37);
		put(KeyEvent.VK_L, 38);
		put(KeyEvent.VK_SEMICOLON, 39);
		put(KeyEvent.VK_QUOTE, 40);
		put(KeyEvent.VK_DEAD_GRAVE, 41);
		put(KeyEvent.VK_SHIFT, 42);
		put(KeyEvent.VK_BACK_SLASH, 43);
		put(KeyEvent.VK_Z, 44);
		put(KeyEvent.VK_X, 45);
		put(KeyEvent.VK_C, 46);
		put(KeyEvent.VK_V, 47);
		put(KeyEvent.VK_B, 48);
		put(KeyEvent.VK_N, 49);
		put(KeyEvent.VK_M, 50);
		put(KeyEvent.VK_COMMA, 51);
		put(KeyEvent.VK_PERIOD, 52);
		put(KeyEvent.VK_SLASH, 53);
		/* RIGHT_SHIFT */
		put(KeyEvent.VK_MULTIPLY, 55);
		put(KeyEvent.VK_ALT, 56);
		put(KeyEvent.VK_SPACE, 57);
		put(KeyEvent.VK_CAPS_LOCK, 58);
		put(KeyEvent.VK_F1, 59);
		put(KeyEvent.VK_F2, 60);
		put(KeyEvent.VK_F3, 61);
		put(KeyEvent.VK_F4, 62);
		put(KeyEvent.VK_F5, 63);
		put(KeyEvent.VK_F6, 64);
		put(KeyEvent.VK_F7, 65);
		put(KeyEvent.VK_F8, 66);
		put(KeyEvent.VK_F9, 67);
		put(KeyEvent.VK_F10, 68);
		put(KeyEvent.VK_NUM_LOCK, 69);
		put(KeyEvent.VK_SCROLL_LOCK, 70);
		/* KP7-9 */
		put(KeyEvent.VK_SUBTRACT, 74);
		/* KP4-6 */
		put(KeyEvent.VK_ADD, 78);
		/* KP1-3 & 0 */
		put(KeyEvent.VK_DECIMAL, 83);
		put(KeyEvent.VK_F11, 87);
		put(KeyEvent.VK_F12, 88);
		put(KeyEvent.VK_F13, 100);
		put(KeyEvent.VK_F14, 101);
		put(KeyEvent.VK_F15, 102);
		/* KP_EQUAL */
		/* KP_ENTER */
		/* RIGHT_CTRL */
		put(KeyEvent.VK_DIVIDE, 181);
		/* RIGHT_ALT */
		put(KeyEvent.VK_PAUSE, 197);
		put(KeyEvent.VK_HOME, 199);
		put(KeyEvent.VK_UP, 200);
		put(KeyEvent.VK_PAGE_UP, 201);
		put(KeyEvent.VK_LEFT, 203);
		put(KeyEvent.VK_RIGHT, 205);
		put(KeyEvent.VK_END, 207);
		put(KeyEvent.VK_DOWN, 208);
		put(KeyEvent.VK_PAGE_DOWN, 209);
		put(KeyEvent.VK_INSERT, 210);
		put(KeyEvent.VK_DELETE, 211);
	}});
	
	public static int translateToCC(int keycode) {
		return swingToCCMap.getOrDefault(keycode, 0);
	}

	public static int translateToSwing(int keycode) {
		return swingToCCMap.inverse().getOrDefault(keycode, 0);
	}
}
