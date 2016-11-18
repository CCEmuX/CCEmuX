package net.clgd.ccemux.emulation.tror

class BlitLinePacket extends TRoRPacket<LineData> {
	public new(String text, String fg, String bg) {
		if(text.length != fg.length || fg.length != bg.length) throw new IllegalArgumentException(
			"All arguments must be same length")
		data = new LineData(text, fg, bg)
	}

	override getPacketCode() {
		"TY"
	}
}
