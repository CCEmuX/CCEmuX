package net.clgd.ccemux.emulation.tror

class CapabilitiesPacket(val caps: Set<String>) : TRoRPacket<Set<String>>(caps) {
	override val packetCode: String = "SP"
}
