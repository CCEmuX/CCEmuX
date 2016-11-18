package net.clgd.ccemux.emulation.tror

import java.util.Set

class CapabilitiesPacket extends TRoRPacket<Set<String>> {
	public new(Set<String> capabilities) {
		data = capabilities
	}
	
	override getPacketCode() {
		"SP"
	}
	
	override toString(String metadata) {
		"SP:" + metadata + ";" + ('-' + data.reduce[$1 + '-' + $0] + '-') + '\n'
	}
	
	override toString() {
		toString("")
	}
}