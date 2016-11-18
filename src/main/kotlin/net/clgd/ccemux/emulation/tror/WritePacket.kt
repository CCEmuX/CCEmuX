package net.clgd.ccemux.emulation.tror

class WritePacket(val text: String) : TRoRPacket<String>(text) {
	override val packetCode: String = "TW"
}
