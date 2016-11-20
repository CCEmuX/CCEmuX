package net.clgd.ccemux.emulation.tror;

public class ClearLinePacket extends TRoRPacket<Void> {
	@Override
	public String getPacketCode() {
		return "TL";
	}
}
