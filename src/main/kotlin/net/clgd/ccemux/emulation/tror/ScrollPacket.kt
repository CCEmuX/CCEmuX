package net.clgd.ccemux.emulation.tror

class ScrollPacket(lines: Int) : TRoRPacket<Int>(lines) {
	override val packetCode: String = "TS"
}
