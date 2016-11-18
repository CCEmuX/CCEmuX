package net.clgd.ccemux.emulation.tror

import net.clgd.ccemux.Utils.BASE_16

abstract class ColourPacket(val colour: Char) : TRoRPacket<Char>(colour) {
	init {
		if (!BASE_16.contains("" + colour)) {
			throw IllegalArgumentException("Invalid colour code " + colour)
		}
	}
}
