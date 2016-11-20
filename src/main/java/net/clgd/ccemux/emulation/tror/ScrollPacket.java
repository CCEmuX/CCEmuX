package net.clgd.ccemux.emulation.tror;

public class ScrollPacket extends TRoRPacket<Integer> {
	public ScrollPacket(int lines) {
		data = lines;
	}
	
	@Override
	public String getPacketCode() {
		return "TS";
	}
}
