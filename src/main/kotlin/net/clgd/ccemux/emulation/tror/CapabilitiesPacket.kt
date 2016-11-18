package net.clgd.ccemux.emulation.tror

class CapabilitiesPacket(val caps: Set<String>) : TRoRPacket<Set<String>>(caps) {
	override val packetCode: String = "SP"

	override fun toString(metadata: String) = "SP:%s;-%s-\n".format(metadata, contents.reduce { a, b -> a + '-' + b })

	override fun toString() = toString("")
}
