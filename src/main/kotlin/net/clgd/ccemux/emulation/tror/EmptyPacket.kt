package net.clgd.ccemux.emulation.tror

abstract class EmptyPacket : TRoRPacket<EmptyPacket.NoData>(NoData()) {
	class NoData {
		override fun toString() = ""
	}
}
