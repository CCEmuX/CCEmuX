package net.clgd.ccemux.emulation.tror

class BackgroundColourPacket(char: Char) : ColourPacket(char) {
	override val packetCode: String = "TK"
}
