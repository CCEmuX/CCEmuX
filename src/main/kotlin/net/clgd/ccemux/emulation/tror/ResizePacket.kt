package net.clgd.ccemux.emulation.tror

class ResizePacket(val width: Int, val height: Int) : TRoRPacket<IntPair>(IntPair(width, height)) {
	override val packetCode: String = "TR"
}
