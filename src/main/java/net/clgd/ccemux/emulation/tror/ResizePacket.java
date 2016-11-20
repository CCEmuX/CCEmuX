package net.clgd.ccemux.emulation.tror;

public class ResizePacket extends TRoRPacket<IntPair> {
	public ResizePacket(int width, int height) {
		data = new IntPair(width, height);
	}
	
	@Override
	public String getPacketCode() {
		return "TR";
	}
}
