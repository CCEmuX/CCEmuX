package net.clgd.ccemux.emulation.tror

import dan200.computercraft.core.terminal.Terminal
import java.util.LinkedList
import net.clgd.ccemux.emulation.CCEmuX
import org.eclipse.xtend.lib.annotations.Accessors

import static net.clgd.ccemux.Utils.BASE_16

class TRoRTerminal extends Terminal {
	@Accessors(PUBLIC_GETTER) val trorQueue = new LinkedList<TRoRPacket<? extends Object>>()

	val CCEmuX emu

	new(CCEmuX emu, int termWidth, int termHeight) {
		super(termWidth, termHeight)
		this.emu = emu
	}

	protected def queue(TRoRPacket<? extends Object> packet) {
		synchronized (trorQueue) {
			trorQueue.add(packet)
		}
	}

	def popQueue() {
		synchronized (trorQueue) {
			val out = trorQueue.clone as LinkedList<TRoRPacket<? extends Object>>
			trorQueue.clear
			return out
		}
	}

	override resize(int w, int h) {
		super.resize(w, h)
		queue(new ResizePacket(w, h))
	}

	override setCursorPos(int x, int y) {
		super.setCursorPos(x, y)
		queue(new CursorPosPacket(x, y))
	}

	override setCursorBlink(boolean blink) {
		super.setCursorBlink(blink)
		queue(new CursorBlinkPacket(blink))
	}

	override setTextColour(int c) {
		super.setTextColour(c)
		queue(new TextColorPacket(BASE_16.charAt(c)))
	}

	override setBackgroundColour(int c) {
		super.setBackgroundColour(c)
		queue(new BackgroundColorPacket(BASE_16.charAt(c)))
	}

	override blit(String text, String fg, String bg) {
		super.blit(text, fg, bg)
		val y = cursorY
		queue(
			new BlitLinePacket(getLine(y).toString, getTextColourLine(y).toString, getBackgroundColourLine(y).toString))
	}

	override write(String text) {
		super.write(text)
		queue(new WritePacket(text))
	}

	override scroll(int lines) {
		super.scroll(lines)
		queue(new ScrollPacket(lines))
	}

	override clear() {
		super.clear
		queue(new ClearPacket)
	}

	override clearLine() {
		super.clearLine
		queue(new ClearLinePacket)
	}

	override setLine(int line, String text, String fg, String bg) {
		super.setLine(line, text, fg, bg)
		queue(new CursorPosPacket(1, line))
		queue(new BlitLinePacket(text, fg, bg))
	}
}
