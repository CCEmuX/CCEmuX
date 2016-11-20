package net.clgd.ccemux.emulation.tror;

public class ConnectionClosedPacket extends TRoRPacket<String> {
	public ConnectionClosedPacket(String reason) {
		data = reason;
	}
	
	@Override
	public String getPacketCode() {
		return "SC";
	}
}
