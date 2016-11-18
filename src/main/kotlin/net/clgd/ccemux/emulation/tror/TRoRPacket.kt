package net.clgd.ccemux.emulation.tror

abstract class TRoRPacket<T>(val contents: T) {
	abstract val packetCode: String

	open fun toString(metadata: String) = packetCode + ':' + metadata + ';' + contents.toString() + '\n'

	override fun toString() = toString("")

	override fun equals(other: Any?): Boolean {
		if (other == this) return true
		if (other == null) return false

		val contents = contents

		if (other is TRoRPacket<*> && contents != null) {
			return packetCode == other.packetCode && contents == other.contents
		}

		return false
	}

	override fun hashCode() = (packetCode.hashCode() + 3) * (contents?.hashCode() ?: 0)
}
