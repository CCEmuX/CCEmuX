package net.clgd.ccemux.rendering;

import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer

enum class RenderingMethods(val factory: (CCEmuX, EmulatedComputer) -> Renderer) {
	Headless({ emu, comp -> BlankRenderer() }),

	AWT(AWTRenderer::new),
	TRoR_STDIO(TRoRRenderer::new);

	fun create(emu: CCEmuX, computer: EmulatedComputer) = factory(emu, computer)
}
