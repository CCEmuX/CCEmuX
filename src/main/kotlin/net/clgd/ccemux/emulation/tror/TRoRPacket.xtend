package net.clgd.ccemux.emulation.tror

import org.eclipse.xtend.lib.annotations.Accessors

abstract class TRoRPacket<T> {
	@Accessors(PUBLIC_GETTER, PROTECTED_SETTER) var T data
	
	abstract def String getPacketCode()
	
	def String toString(String metadata) {
		packetCode + ':' + metadata + ';' + (data ?: "").toString + '\n'
	}
	
	override toString() {
		toString('')
	}
	
	override equals(Object o) {
		if (o == this) return true
		if (o == null) return false
		
		if (o instanceof TRoRPacket<?>) {
			val p = o as TRoRPacket<?>
			return packetCode == p.packetCode && data?.equals(p.data)
		}
		return false
	}
	
	override hashCode() {
		(packetCode.hashCode + 3) * data?.hashCode
	}
}