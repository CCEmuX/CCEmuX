package net.clgd.ccemux.emulation.tror;

public class WritePacket extends TRoRPacket<String> {
	public WritePacket(String text) {
		data = text;
	}
	
	@Override
	public String getPacketCode() {
		return "TW";
	}
}
