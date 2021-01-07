package net.clgd.ccemux.rendering.awt;

import java.awt.event.KeyEvent;

import com.google.common.collect.ImmutableMap;

public class KeyTranslator {
	private static final ImmutableMap<Integer, Integer> swingToCCMap = ImmutableMap.<Integer, Integer>builder()
		.put(KeyEvent.VK_SPACE, 32)
		.put(KeyEvent.VK_QUOTE, 39)
		.put(KeyEvent.VK_COMMA, 44)
		.put(KeyEvent.VK_MINUS, 45)
		.put(KeyEvent.VK_PERIOD, 46)
		.put(KeyEvent.VK_SLASH, 47)
		.put(KeyEvent.VK_0, 48)
		.put(KeyEvent.VK_1, 49)
		.put(KeyEvent.VK_2, 50)
		.put(KeyEvent.VK_3, 51)
		.put(KeyEvent.VK_4, 52)
		.put(KeyEvent.VK_5, 53)
		.put(KeyEvent.VK_6, 54)
		.put(KeyEvent.VK_7, 55)
		.put(KeyEvent.VK_8, 56)
		.put(KeyEvent.VK_9, 57)
		.put(KeyEvent.VK_SEMICOLON, 59)
		.put(KeyEvent.VK_EQUALS, 61)
		.put(KeyEvent.VK_A, 65)
		.put(KeyEvent.VK_B, 66)
		.put(KeyEvent.VK_C, 67)
		.put(KeyEvent.VK_D, 68)
		.put(KeyEvent.VK_E, 69)
		.put(KeyEvent.VK_F, 70)
		.put(KeyEvent.VK_G, 71)
		.put(KeyEvent.VK_H, 72)
		.put(KeyEvent.VK_I, 73)
		.put(KeyEvent.VK_J, 74)
		.put(KeyEvent.VK_K, 75)
		.put(KeyEvent.VK_L, 76)
		.put(KeyEvent.VK_M, 77)
		.put(KeyEvent.VK_N, 78)
		.put(KeyEvent.VK_O, 79)
		.put(KeyEvent.VK_P, 80)
		.put(KeyEvent.VK_Q, 81)
		.put(KeyEvent.VK_R, 82)
		.put(KeyEvent.VK_S, 83)
		.put(KeyEvent.VK_T, 84)
		.put(KeyEvent.VK_U, 85)
		.put(KeyEvent.VK_V, 86)
		.put(KeyEvent.VK_W, 87)
		.put(KeyEvent.VK_X, 88)
		.put(KeyEvent.VK_Y, 89)
		.put(KeyEvent.VK_Z, 90)
		.put(KeyEvent.VK_OPEN_BRACKET, 91)
		.put(KeyEvent.VK_BACK_SLASH, 92)
		.put(KeyEvent.VK_CLOSE_BRACKET, 93)
		.put(KeyEvent.VK_BACK_QUOTE, 96)
		.put(KeyEvent.VK_ENTER, 257)
		.put(KeyEvent.VK_TAB, 258)
		.put(KeyEvent.VK_BACK_SPACE, 259)
		.put(KeyEvent.VK_INSERT, 260)
		.put(KeyEvent.VK_DELETE, 261)
		.put(KeyEvent.VK_RIGHT, 262)
		.put(KeyEvent.VK_LEFT, 263)
		.put(KeyEvent.VK_DOWN, 264)
		.put(KeyEvent.VK_UP, 265)
		.put(KeyEvent.VK_PAGE_UP, 266)
		.put(KeyEvent.VK_PAGE_DOWN, 267)
		.put(KeyEvent.VK_HOME, 268)
		.put(KeyEvent.VK_END, 269)
		.put(KeyEvent.VK_CAPS_LOCK, 280)
		.put(KeyEvent.VK_SCROLL_LOCK, 281)
		.put(KeyEvent.VK_NUM_LOCK, 282)
		.put(KeyEvent.VK_PAUSE, 284)
		.put(KeyEvent.VK_F1, 290)
		.put(KeyEvent.VK_F2, 291)
		.put(KeyEvent.VK_F3, 292)
		.put(KeyEvent.VK_F4, 293)
		.put(KeyEvent.VK_F5, 294)
		.put(KeyEvent.VK_F6, 295)
		.put(KeyEvent.VK_F7, 296)
		.put(KeyEvent.VK_F8, 297)
		.put(KeyEvent.VK_F9, 298)
		.put(KeyEvent.VK_F10, 299)
		.put(KeyEvent.VK_F11, 300)
		.put(KeyEvent.VK_F12, 301)
		.put(KeyEvent.VK_F13, 302)
		.put(KeyEvent.VK_F14, 303)
		.put(KeyEvent.VK_F15, 304)
		.put(KeyEvent.VK_F16, 305)
		.put(KeyEvent.VK_F17, 306)
		.put(KeyEvent.VK_F18, 307)
		.put(KeyEvent.VK_F19, 308)
		.put(KeyEvent.VK_F20, 309)
		.put(KeyEvent.VK_F21, 310)
		.put(KeyEvent.VK_F22, 311)
		.put(KeyEvent.VK_F23, 312)
		.put(KeyEvent.VK_F24, 313)
		.put(KeyEvent.VK_NUMPAD0, 320)
		.put(KeyEvent.VK_NUMPAD1, 321)
		.put(KeyEvent.VK_NUMPAD2, 322)
		.put(KeyEvent.VK_NUMPAD3, 323)
		.put(KeyEvent.VK_NUMPAD4, 324)
		.put(KeyEvent.VK_NUMPAD5, 325)
		.put(KeyEvent.VK_NUMPAD6, 326)
		.put(KeyEvent.VK_NUMPAD7, 327)
		.put(KeyEvent.VK_NUMPAD8, 328)
		.put(KeyEvent.VK_NUMPAD9, 329)
		.put(KeyEvent.VK_SHIFT, 340)
		.put(KeyEvent.VK_CONTROL, 341)
		.put(KeyEvent.VK_ALT, 342)
		.put(KeyEvent.VK_ALT_GRAPH, 346)
		.build();

	private static final ImmutableMap<Integer, Integer> swingRightToCCMap = ImmutableMap.<Integer, Integer>builder()
		.put(KeyEvent.VK_SHIFT, 344)
		.put(KeyEvent.VK_CONTROL, 345)
		.put(KeyEvent.VK_ALT, 346)
		.build();


	private static final ImmutableMap<Integer, Integer> swingNumpadToCC = ImmutableMap.<Integer, Integer>builder()
		.put(KeyEvent.VK_PERIOD, 330)
		.put(KeyEvent.VK_DIVIDE, 331)
		.put(KeyEvent.VK_SUBTRACT, 333)
		.put(KeyEvent.VK_ADD, 334)
		.put(KeyEvent.VK_ENTER, 335)
		.put(KeyEvent.VK_EQUALS, 336)
		.build();

	public static int translateToCC(int keycode, int location) {
		Integer code;
		switch (location)
		{
			case KeyEvent.KEY_LOCATION_RIGHT:
				code = swingRightToCCMap.get(keycode);
				break;
			case KeyEvent.KEY_LOCATION_NUMPAD:
				code = swingNumpadToCC.get(keycode);
				break;
			default:
				code = null;
				break;
		}

		return code != null ? code : swingToCCMap.getOrDefault(keycode, -1);
	}
}
