package net.clgd.ccemux.emulation.tror;

public class ClearPacket extends TRoRPacket<Void> {
	@Override
	public String getPacketCode() {
		return "TE";
	}
}
