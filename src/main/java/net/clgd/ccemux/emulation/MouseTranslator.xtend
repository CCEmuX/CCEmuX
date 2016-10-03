package net.clgd.ccemux.emulation

import java.awt.event.MouseEvent

class MouseTranslator {
	static def mouseButtonToCC(int button) {
		switch (button) {
			case MouseEvent.BUTTON1: // Left button
				return 1
				
			case MouseEvent.BUTTON2: // Middle button
				return 3
				
			case MouseEvent.BUTTON3: // Right button
				return 2
		}
		
		return 4
	}
}