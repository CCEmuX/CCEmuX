package net.clgd.ccemux.emulation

import java.awt.event.MouseEvent

class MouseTranslator {
	static def swingToCC(int button) {
		return switch (button) {
			case MouseEvent.BUTTON1: 1
			case MouseEvent.BUTTON2: 3
			case MouseEvent.BUTTON3: 2
			default: -1
		}
	}

	static def ccToSwing(int button) {
		return switch (button) {
			case 1: MouseEvent.BUTTON1
			case 2: MouseEvent.BUTTON3
			case 3: MouseEvent.BUTTON2
			default: -1
		}
	}
}