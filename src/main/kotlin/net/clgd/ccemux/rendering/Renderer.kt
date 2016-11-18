package net.clgd.ccemux.rendering

import net.clgd.ccemux.emulation.EmulatedComputer

interface Renderer : EmulatedComputer.Listener {
	fun isVisible(): Boolean
	fun setVisible(visible: Boolean)

	fun resize(width: Int, height: Int)
}
