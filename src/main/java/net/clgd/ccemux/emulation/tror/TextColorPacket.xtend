package net.clgd.ccemux.emulation.tror

import net.clgd.ccemux.Utils

class TextColorPacket extends TRoRPacket<Character> {
	public new(char color) {
		if(!Utils.BASE_16.contains('' + color)) throw new IllegalArgumentException("Invalid color code " + color)
		data = color
	}

	override getPacketCode() {
		"TF"
	}
}
