package net.clgd.ccemux.emulation.tror

class TextColourPacket(colour: Char) : ColourPacket(colour) {
	override val packetCode: String = "TF"
}
