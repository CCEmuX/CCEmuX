package net.clgd.ccemux.emulation.tror

class CursorPosPacket(val x: Int, val y: Int) : TRoRPacket<Pair<Int, Int>>(Pair(x, y)) {
	override val packetCode: String = "TC"
}
