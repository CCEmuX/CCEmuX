package net.clgd.ccemux.rendering.javafx;

import static javafx.scene.input.KeyCode.*;

import com.google.common.collect.ImmutableBiMap;
import javafx.scene.input.KeyCode;

public class JFXKeyTranslator {
	private static final ImmutableBiMap<KeyCode, Integer> jfxToCC = ImmutableBiMap.<KeyCode, Integer>builder()
		.put(DIGIT1, 2).put(DIGIT2, 3).put(DIGIT3, 4).put(DIGIT4, 5).put(DIGIT5, 6).put(DIGIT6, 7).put(DIGIT7, 8)
		.put(DIGIT8, 9).put(DIGIT9, 10).put(DIGIT0, 11).put(MINUS, 12).put(EQUALS, 13).put(BACK_SPACE, 14)
		.put(TAB, 15).put(Q, 16).put(W, 17).put(E, 18).put(R, 19).put(T, 20).put(Y, 21).put(U, 22).put(I, 23)
		.put(O, 24).put(P, 25).put(OPEN_BRACKET, 26).put(CLOSE_BRACKET, 27).put(ENTER, 28).put(CONTROL, 29)
		.put(A, 30).put(S, 31).put(D, 32).put(F, 33).put(G, 34).put(H, 35).put(J, 36).put(K, 37).put(L, 38)
		.put(SEMICOLON, 39).put(QUOTE, 40).put(DEAD_GRAVE, 41).put(SHIFT, 42).put(BACK_SLASH, 43).put(Z, 44)
		.put(X, 45).put(C, 46).put(V, 47).put(B, 48).put(N, 49).put(M, 50).put(COMMA, 51).put(PERIOD, 52)
		.put(SLASH, 53).put(MULTIPLY, 55).put(ALT, 56).put(SPACE, 57).put(CAPS, 58).put(F1, 59).put(F2, 60)
		.put(F3, 61).put(F4, 62).put(F5, 63).put(F6, 64).put(F7, 65).put(F8, 66).put(F9, 67).put(F10, 68)
		.put(NUM_LOCK, 69).put(SCROLL_LOCK, 70).put(SUBTRACT, 74).put(ADD, 78).put(DECIMAL, 83).put(F11, 87)
		.put(F12, 88).put(F13, 100).put(F14, 101).put(F15, 102).put(DIVIDE, 181).put(PAUSE, 197).put(HOME, 199)
		.put(UP, 200).put(PAGE_UP, 201).put(LEFT, 203).put(RIGHT, 205).put(END, 207).put(DOWN, 208)
		.put(PAGE_DOWN, 209).put(INSERT, 210).put(DELETE, 211).put(WINDOWS, 219).put(PRINTSCREEN, 183)
		.put(KeyCode.NUMPAD1, 79).put(KeyCode.NUMPAD2, 80).put(KeyCode.NUMPAD3, 81).put(KeyCode.NUMPAD4, 75)
		.put(KeyCode.NUMPAD5, 76).put(KeyCode.NUMPAD6, 77).put(KeyCode.NUMPAD7, 71).put(KeyCode.NUMPAD8, 72)
		.put(KeyCode.NUMPAD9, 73).put(KeyCode.NUMPAD0, 82).build();

	public static int translateToCC(KeyCode code) {
		return jfxToCC.getOrDefault(code, 0);
	}

	public static KeyCode translateToJFX(int keycode) {
		return jfxToCC.inverse().getOrDefault(keycode, KeyCode.UNDEFINED);
	}
}
