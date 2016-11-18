package net.clgd.ccemux.rendering

class BlankRenderer : Renderer {
	override fun onUpdate(dt: Float) { }

	override fun onDispose() { }

	override fun onTerminalResized(width: Int, height: Int) { }

	override fun isVisible(): Boolean { }

	override fun setVisible(visible: Boolean) { }

	override fun resize(width: Int, height: Int) { }
}
