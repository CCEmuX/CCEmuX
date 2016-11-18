package net.clgd.ccemux.emulation.tror

class CursorPosPacket(val x: Int, val y: Int) : TRoRPacket<IntPair>(IntPair(x, y)) {
	override val packetCode: String = "TC"
}
