package net.clgd.ccemux.rendering

import net.clgd.ccemux.emulation.EmulatedComputer

interface Renderer extends EmulatedComputer.Listener {
	def boolean isVisible()
	def void setVisible(boolean visible)

	def void resize(int width, int height)
}