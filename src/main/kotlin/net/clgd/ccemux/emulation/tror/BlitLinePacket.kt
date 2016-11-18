package net.clgd.ccemux.emulation.tror

class BlitLinePacket(val text: String, val fg: String, val bg: String) : TRoRPacket<LineData>(LineData(text, fg, bg)) {
	override val packetCode: String = "TY"

	init {
		if (text.length != fg.length || fg.length != bg.length) {
			throw IllegalArgumentException("All arguments must have the same length")
		}
	}
}
