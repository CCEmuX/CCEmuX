package net.clgd.ccemux.emulation.tror

import dan200.computercraft.core.terminal.Terminal
import java.util.LinkedList
import org.eclipse.xtend.lib.annotations.Accessors
import net.clgd.ccemux.emulation.CCEmuX

class TRoRTerminal extends Terminal {
	@Accessors(PUBLIC_GETTER) val trorQueue = new LinkedList<String>()

	val CCEmuX emu

	new(CCEmuX emu, int termWidth, int termHeight) {
		super(termWidth, termHeight)
		this.emu = emu
	}

	private def queue(String s) {
		synchronized (trorQueue) {
			trorQueue.add(s)
		}
	}

	override setCursorPos(int x, int y) {
		super.setCursorPos(x, y)
		
		
	}
}
