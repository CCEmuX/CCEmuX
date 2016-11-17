package net.clgd.ccemux.emulation.tror

import net.clgd.ccemux.emulation.tror.TRoRPacket

class CursorPosPacket extends TRoRPacket<IntPair> {
	public new(int x, int y) {
		data = new IntPair(x, y)
	}
	
	override getPacketCode() {
		"TC"
	}
	
}