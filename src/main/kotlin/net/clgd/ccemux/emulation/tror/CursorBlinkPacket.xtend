package net.clgd.ccemux.emulation.tror

class CursorBlinkPacket extends TRoRPacket<Boolean> {
	public new(boolean blink) {
		data = blink
	}
	
	override getPacketCode() {
		"TB"
	}
}