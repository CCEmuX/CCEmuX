package net.clgd.ccemux.emulation.tror;

public class BlitLinePacket extends TRoRPacket<LineData> {
	public BlitLinePacket(String text, String fg, String bg) {
		data = new LineData(text, fg, bg);
	}
	
	@Override
	public String getPacketCode() {
		return "TY";
	}
}
