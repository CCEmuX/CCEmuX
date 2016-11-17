package net.clgd.ccemux.emulation.tror

class BlitLinePacket extends TRoRPacket<LineData> {
	public new(String text, String fg, String bg) {
		data = new LineData(text, fg, bg)
	}
	
	override getPacketCode() {
		"TY"
	}
}