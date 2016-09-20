package net.clgd.ccemux.emulation

import java.awt.event.KeyEvent

class KeyTranslator {
	static val swingToCCMap = #{
		KeyEvent.VK_1 -> 2,
		KeyEvent.VK_2 -> 3,
		KeyEvent.VK_3 -> 4,
		KeyEvent.VK_4 -> 5,
		KeyEvent.VK_5 -> 6,
		KeyEvent.VK_6 -> 7,
		KeyEvent.VK_7 -> 8,
		KeyEvent.VK_8 -> 9,
		KeyEvent.VK_9 -> 10,
		KeyEvent.VK_0 -> 11,
		KeyEvent.VK_MINUS -> 12,
		KeyEvent.VK_EQUALS -> 13,
		KeyEvent.VK_BACK_SPACE -> 14,
		KeyEvent.VK_TAB -> 15,
		KeyEvent.VK_Q -> 16,
		KeyEvent.VK_W -> 17,
		KeyEvent.VK_E -> 18,
		KeyEvent.VK_R -> 19,
		KeyEvent.VK_T -> 20,
		KeyEvent.VK_Y -> 21,
		KeyEvent.VK_U -> 22,
		KeyEvent.VK_I -> 23,
		KeyEvent.VK_O -> 24,
		KeyEvent.VK_P -> 25,
		KeyEvent.VK_BRACELEFT -> 26,
		KeyEvent.VK_BRACERIGHT -> 27,
		KeyEvent.VK_ENTER -> 28,
		KeyEvent.VK_CONTROL -> 29,
		KeyEvent.VK_A -> 30,
		KeyEvent.VK_S -> 31,
		KeyEvent.VK_D -> 32,
		KeyEvent.VK_F -> 33,
		KeyEvent.VK_G -> 34,
		KeyEvent.VK_H -> 35,
		KeyEvent.VK_J -> 36,
		KeyEvent.VK_K -> 37,
		KeyEvent.VK_L -> 38,
		KeyEvent.VK_SEMICOLON -> 39,
		KeyEvent.VK_QUOTE -> 40,
		KeyEvent.VK_DEAD_GRAVE -> 41,
		KeyEvent.VK_SHIFT -> 42,
		KeyEvent.VK_BACK_SLASH -> 43,
		KeyEvent.VK_Z -> 44,
		KeyEvent.VK_X -> 45,
		KeyEvent.VK_C -> 46,
		KeyEvent.VK_V -> 47,
		KeyEvent.VK_B -> 48,
		KeyEvent.VK_N -> 49,
		KeyEvent.VK_M -> 50,
		KeyEvent.VK_COMMA -> 51,
		KeyEvent.VK_PERIOD -> 52,
		KeyEvent.VK_SLASH -> 53,
		/* RIGHT_SHIFT */
		KeyEvent.VK_MULTIPLY -> 55,
		KeyEvent.VK_ALT -> 56,
		KeyEvent.VK_SPACE -> 57,
		KeyEvent.VK_CAPS_LOCK -> 58,
		KeyEvent.VK_F1 -> 59,
		KeyEvent.VK_F2 -> 60,
		KeyEvent.VK_F3 -> 61,
		KeyEvent.VK_F4 -> 62,
		KeyEvent.VK_F5 -> 63,
		KeyEvent.VK_F6 -> 64,
		KeyEvent.VK_F7 -> 65,
		KeyEvent.VK_F8 -> 66,
		KeyEvent.VK_F9 -> 67,
		KeyEvent.VK_F10 -> 68,
		KeyEvent.VK_NUM_LOCK -> 69,
		KeyEvent.VK_SCROLL_LOCK -> 70,
		/* KP7-9 */
		KeyEvent.VK_SUBTRACT -> 74,
		/* KP4-6 */
		KeyEvent.VK_ADD -> 78,
		/* KP1-3 & 0 */
		KeyEvent.VK_DECIMAL -> 83,
		KeyEvent.VK_F11 -> 87,
		KeyEvent.VK_F12 -> 88,
		KeyEvent.VK_F13 -> 100,
		KeyEvent.VK_F14 -> 101,
		KeyEvent.VK_F15 -> 102,
		/* KP_EQUAL */
		/* KP_ENTER */
		/* RIGHT_CTRL */
		KeyEvent.VK_DIVIDE -> 181,
		/* RIGHT_ALT */
		KeyEvent.VK_PAUSE -> 197,
		KeyEvent.VK_HOME -> 199,
		KeyEvent.VK_UP -> 200,
		KeyEvent.VK_PAGE_UP -> 201,
		KeyEvent.VK_LEFT -> 203,
		KeyEvent.VK_RIGHT -> 205,
		KeyEvent.VK_END -> 207,
		KeyEvent.VK_DOWN -> 209,
		KeyEvent.VK_INSERT -> 210,
		KeyEvent.VK_DELETE -> 211
	}
	
	static def translateToCC(int keycode) {
		return if (swingToCCMap.containsKey(keycode)) {
			swingToCCMap.get(keycode)	
		} else {
			0
		}
	}
	
    static def translateToSwing(int keycode) {
        for (k : swingToCCMap.keySet) {
            if (swingToCCMap.get(k) == keycode) {
                return k
            }
        }
        
        return 0
    }
}