package net.clgd.ccemux.emulation.tror

class WritePacket extends TRoRPacket<String> {
	public new(String text) {
		data = text
	}
	
	override getPacketCode() {
		"TW"
	}
}