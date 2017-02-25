package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.Input;
import com.google.common.collect.ImmutableBiMap;

import java.util.HashMap;

public class KeyTranslator {
	@SuppressWarnings("serial")
	private static final ImmutableBiMap<Integer, Integer> gdxToCCMap = ImmutableBiMap.copyOf(new HashMap<Integer,
		Integer>() {{
		put(Input.Keys.NUM_1, 2);
		put(Input.Keys.NUM_2, 3);
		put(Input.Keys.NUM_3, 4);
		put(Input.Keys.NUM_4, 5);
		put(Input.Keys.NUM_5, 6);
		put(Input.Keys.NUM_6, 7);
		put(Input.Keys.NUM_7, 8);
		put(Input.Keys.NUM_8, 9);
		put(Input.Keys.NUM_9, 10);
		put(Input.Keys.NUM_0, 11);
		put(Input.Keys.MINUS, 12);
		put(Input.Keys.EQUALS, 13);
		put(Input.Keys.BACKSPACE, 14);
		put(Input.Keys.TAB, 15);
		put(Input.Keys.Q, 16);
		put(Input.Keys.W, 17);
		put(Input.Keys.E, 18);
		put(Input.Keys.R, 19);
		put(Input.Keys.T, 20);
		put(Input.Keys.Y, 21);
		put(Input.Keys.U, 22);
		put(Input.Keys.I, 23);
		put(Input.Keys.O, 24);
		put(Input.Keys.P, 25);
		put(Input.Keys.LEFT_BRACKET, 26);
		put(Input.Keys.RIGHT_BRACKET, 27);
		put(Input.Keys.ENTER, 28);
		put(Input.Keys.CONTROL_LEFT, 29);
		put(Input.Keys.A, 30);
		put(Input.Keys.S, 31);
		put(Input.Keys.D, 32);
		put(Input.Keys.F, 33);
		put(Input.Keys.G, 34);
		put(Input.Keys.H, 35);
		put(Input.Keys.J, 36);
		put(Input.Keys.K, 37);
		put(Input.Keys.L, 38);
		put(Input.Keys.SEMICOLON, 39);
		/* QUOTE */
		/* DEAD_GRAVE */
		put(Input.Keys.SHIFT_LEFT, 42);
		put(Input.Keys.BACKSLASH, 43);
		put(Input.Keys.Z, 44);
		put(Input.Keys.X, 45);
		put(Input.Keys.C, 46);
		put(Input.Keys.V, 47);
		put(Input.Keys.B, 48);
		put(Input.Keys.N, 49);
		put(Input.Keys.M, 50);
		put(Input.Keys.COMMA, 51);
		put(Input.Keys.PERIOD, 52);
		put(Input.Keys.SLASH, 53);
		put(Input.Keys.SHIFT_RIGHT, 54);
		/* MULTIPLY */
		put(Input.Keys.ALT_LEFT, 56);
		put(Input.Keys.SPACE, 57);
		/* CAPS_LOCK */
		put(Input.Keys.F1, 59);
		put(Input.Keys.F2, 60);
		put(Input.Keys.F3, 61);
		put(Input.Keys.F4, 62);
		put(Input.Keys.F5, 63);
		put(Input.Keys.F6, 64);
		put(Input.Keys.F7, 65);
		put(Input.Keys.F8, 66);
		put(Input.Keys.F9, 67);
		put(Input.Keys.F10, 68);
		/* NUM_LOCK */
		/* SCROLL_LOCK */
		put(Input.Keys.NUMPAD_7, 71);
		put(Input.Keys.NUMPAD_8, 72);
		put(Input.Keys.NUMPAD_9, 73);
		put(Input.Keys.MINUS, 74);
		put(Input.Keys.NUMPAD_4, 75);
		put(Input.Keys.NUMPAD_5, 76);
		put(Input.Keys.NUMPAD_6, 77);
		put(Input.Keys.PLUS, 78);
		put(Input.Keys.NUMPAD_1, 79);
		put(Input.Keys.NUMPAD_2, 80);
		put(Input.Keys.NUMPAD_3, 81);
		/* KP_ENTER */
		/* KP_PERIOD */
		put(Input.Keys.F11, 87);
		put(Input.Keys.F12, 88);
		/* KP_EQUAL */
		/* KP_ENTER */
		put(Input.Keys.CONTROL_RIGHT, 157);
		/* KP_DIVIDE */
		put(Input.Keys.ALT_RIGHT, 184);
		/* PAUSE */
		put(Input.Keys.HOME, 199);
		put(Input.Keys.UP, 200);
		put(Input.Keys.PAGE_UP, 201);
		put(Input.Keys.LEFT, 203);
		put(Input.Keys.RIGHT, 205);
		put(Input.Keys.END, 207);
		put(Input.Keys.DOWN, 208);
		put(Input.Keys.PAGE_DOWN, 209);
		put(Input.Keys.INSERT, 210);
	}});

	public static int translateToCC(int keycode) {
		return gdxToCCMap.getOrDefault(keycode, 0);
	}
}
