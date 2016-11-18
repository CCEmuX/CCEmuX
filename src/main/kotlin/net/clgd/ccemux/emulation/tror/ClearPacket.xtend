package net.clgd.ccemux.emulation.tror

class ClearPacket extends TRoRPacket<Void> {
	override getPacketCode() {
		"TE"
	}
}