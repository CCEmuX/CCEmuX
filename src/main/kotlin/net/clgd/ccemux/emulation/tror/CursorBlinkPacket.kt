package net.clgd.ccemux.emulation.tror

class CursorBlinkPacket(val blink: Boolean) : TRoRPacket<Boolean>(blink) {
	override val packetCode: String = "TB"
}
