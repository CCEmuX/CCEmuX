package net.clgd.ccemux.rendering.javafx;

import javafx.scene.input.MouseButton;

public class JFXMouseTranslator {
	public static int toCC(MouseButton b) {
		switch (b) {
			case PRIMARY:
				return 1;
			case SECONDARY:
				return 2;
			case MIDDLE:
				return 3;
			default:
				return -1;
		}
	}
}
