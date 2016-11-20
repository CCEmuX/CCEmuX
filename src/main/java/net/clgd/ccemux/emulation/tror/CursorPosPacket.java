package net.clgd.ccemux.emulation.tror;

public class CursorPosPacket extends TRoRPacket<IntPair> {
	public CursorPosPacket(int x, int y) {
		data = new IntPair(x, y);
	}
	
	@Override
	public String getPacketCode() {
		return "TC";
	}
}
