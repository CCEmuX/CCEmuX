package net.clgd.ccemux.emulation.tror

class ResizePacket extends TRoRPacket<IntPair> {
	public new(int width, int height) {
		data = new IntPair(width, height)
	}
	
	override getPacketCode() {
		"TR"
	}
	
}