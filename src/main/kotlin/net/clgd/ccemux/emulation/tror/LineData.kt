package net.clgd.ccemux.emulation.tror

data class LineData(val text: String, val fg: String, val bg: String) {
	override fun toString() = "$fg,$bg,$text"
}
