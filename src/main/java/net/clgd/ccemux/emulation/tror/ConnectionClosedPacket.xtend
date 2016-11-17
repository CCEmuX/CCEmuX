package net.clgd.ccemux.emulation.tror

class ConnectionClosedPacket extends TRoRPacket<Void> {
	
	override getPacketCode() {
		"SC"
	}
	
}