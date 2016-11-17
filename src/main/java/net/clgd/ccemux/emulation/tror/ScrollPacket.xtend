package net.clgd.ccemux.emulation.tror

class ScrollPacket extends TRoRPacket<Integer> {
	public new(int lines) {
		data = lines
	}
	
	override getPacketCode() {
		"TS"
	}
}