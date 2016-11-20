package net.clgd.ccemux.emulation.tror;

import java.util.Set;

public class CapabilitiesPacket extends TRoRPacket<Set<String>> {
	public CapabilitiesPacket(Set<String> capabilities) {
		data = capabilities;
	}
	
	@Override
	public String getPacketCode() {
		return "SP";
	}
	
	@Override
	public String toString(String metadata) {
		return "SP:" + metadata + ";-" + (data.stream().reduce((s1, s2) -> s2 + s1)) + "-\n";
	}
	
	@Override
	public String toString() {
		return toString("");
	}
}
