package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.Input;

public class MouseTranslator {
	public static int gdxToCC(int button) {
		switch (button) {
			case Input.Buttons.LEFT:
				return 1;
			case Input.Buttons.RIGHT:
				return 2;
			case Input.Buttons.MIDDLE:
				return 3;
			default:
				return 1;
		}
	}
}
