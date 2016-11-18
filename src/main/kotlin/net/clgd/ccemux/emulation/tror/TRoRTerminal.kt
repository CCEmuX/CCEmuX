package net.clgd.ccemux.emulation.tror

import dan200.computercraft.core.terminal.Terminal
import net.clgd.ccemux.Utils.BASE_16
import net.clgd.ccemux.emulation.CCEmuX
import java.util.*

class TRoRTerminal(val emu: CCEmuX, termWidth: Int, termHeight: Int) : Terminal(termWidth, termHeight) {
	val trorQueue = LinkedList<TRoRPacket<*>>()

	protected fun queue(packet: TRoRPacket<*>) {
		synchronized (trorQueue) {
			trorQueue.add(packet)
		}
	}

	fun popQueue(): LinkedList<TRoRPacket<*>> {
		synchronized (trorQueue) {
			@Suppress("UNCHECKED_CAST") // no other way around this :/
			val out = trorQueue.clone() as LinkedList<TRoRPacket<*>>
			trorQueue.clear()
			return out
		}
	}

	override fun resize(w: Int, h: Int) {
		super.resize(w, h)
		queue(ResizePacket(w, h))
	}

	override fun setCursorPos(x: Int, y: Int) {
		super.setCursorPos(x, y)
		queue(CursorPosPacket(x, y))
	}

	override fun setCursorBlink(blink: Boolean) {
		super.setCursorBlink(blink)
		queue(CursorBlinkPacket(blink))
	}

	override fun setTextColour(c: Int) {
		super.setTextColour(c)
		queue(TextColourPacket(BASE_16.getOrElse(c, { '0' })))
	}

	override fun setBackgroundColour(c: Int) {
		super.setBackgroundColour(c)
		queue(BackgroundColourPacket(BASE_16.getOrElse(c, { 'f' })))
	}

	override fun blit(text: String, fg: String, bg: String) {
		super.blit(text, fg, bg)
		val y = cursorY
		queue(BlitLinePacket(getLine(y).toString(), getTextColourLine(y).toString(), getBackgroundColourLine(y).toString()))
	}

	override fun write(text: String) {
		super.write(text)
		queue(WritePacket(text))
	}

	override fun scroll(lines: Int) {
		super.scroll(lines)
		queue(ScrollPacket(lines))
	}

	override fun clear() {
		super.clear()
		queue(ClearPacket())
	}

	override fun clearLine() {
		super.clearLine()
		queue(ClearLinePacket())
	}

	override fun setLine(line: Int, text: String, fg: String, bg: String) {
		super.setLine(line, text, fg, bg)
		queue(CursorPosPacket(1, line))
		queue(BlitLinePacket(text, fg, bg))
	}
}
