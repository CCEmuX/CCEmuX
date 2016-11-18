package net.clgd.ccemux.rendering.awt

import java.awt.event.MouseEvent

object MouseTranslator {
	fun swingToCC(button: Int): Int {
		return when(button) {
			MouseEvent.BUTTON1 -> 1
			MouseEvent.BUTTON2 -> 3
			MouseEvent.BUTTON3 -> 2
			else -> -1
		}
	}

	fun ccToSwing(button: Int): Int {
		return when(button) {
			1 -> MouseEvent.BUTTON1
			2 -> MouseEvent.BUTTON3
			3 -> MouseEvent.BUTTON2
			else -> -1
		}
	}
}
