package net.clgd.ccemux.emulation.tror

class BackgroundColorPacket extends TRoRPacket<Character> {
	public new(char color) {
		data = color
	}
	
	override getPacketCode() {
		"TK"
	}
}