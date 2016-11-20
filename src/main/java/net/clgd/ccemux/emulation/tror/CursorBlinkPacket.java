package net.clgd.ccemux.emulation.tror;

public class CursorBlinkPacket extends TRoRPacket<Boolean> {
	public CursorBlinkPacket(boolean blink) {
		data = blink;
	}
	
	@Override
	public String getPacketCode() {
		return "TB";
	}
}
