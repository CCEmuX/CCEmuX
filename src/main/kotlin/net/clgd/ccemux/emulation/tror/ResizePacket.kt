package net.clgd.ccemux.emulation.tror

class ResizePacket(val width: Int, val height: Int) : TRoRPacket<Pair<Int, Int>>(Pair(width, height)) {
	override val packetCode: String = "TR"
}
