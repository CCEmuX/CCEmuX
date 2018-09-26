package net.clgd.ccemux.rendering.awt;

import static java.awt.event.MouseEvent.*;

public class MouseTranslator {
	public static int swingToCC(int button) {
		switch (button) {
			case BUTTON1:
				return 1;
			case BUTTON2:
				return 3;
			case BUTTON3:
				return 2;
			default:
				return -1;
		}
	}

	public static int ccToSwing(int button) {
		switch (button) {
			case 1:
				return BUTTON1;
			case 2:
				return BUTTON3;
			case 3:
				return BUTTON2;
			default:
				return -1;
		}
	}
}
