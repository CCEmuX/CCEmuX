package net.clgd.ccemux.emulation.tror

class TextColorPacket extends TRoRPacket<Character> {
	public new(char color) {
		data = color
	}
	
	override getPacketCode() {
		"TF"
	}
}