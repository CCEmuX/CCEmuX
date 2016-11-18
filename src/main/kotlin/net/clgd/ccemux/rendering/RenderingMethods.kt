package net.clgd.ccemux.rendering;

import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.rendering.awt.AWTRenderer
import net.clgd.ccemux.rendering.tror.TRoRRenderer

enum class RenderingMethods(val factory: (CCEmuX, EmulatedComputer) -> Renderer) {
	Headless({ emu, comp -> BlankRenderer() }),
	AWT(::AWTRenderer),
	TRoR_STDIO(::TRoRRenderer);

	fun create(emu: CCEmuX, computer: EmulatedComputer) = factory(emu, computer)
}
