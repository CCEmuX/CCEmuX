package net.clgd.ccemux.emulation

import dan200.computercraft.core.computer.Computer
import dan200.computercraft.core.terminal.Terminal
import java.util.ArrayList
import org.eclipse.xtend.lib.annotations.Accessors
import net.clgd.ccemux.emulation.tror.TRoRTerminal

class EmulatedComputer(val emu: CCEmuX, termWidth: Int, termHeight: Int, id: Int) {
	interface Listener {
		fun onUpdate(dt: Float)
		fun onDispose()

		fun onTerminalResized(width: Int, height: Int)
	}

	val terminal: Terminal = TRoRTerminal(emu, termWidth, termHeight)
	val ccComputer = Computer(emu.env, terminal, -1)

	val cursorChar = '_'

	val listeners = ArrayList<Listener>()

	init {
		ccComputer.id = id
		ccComputer.assignID()

		if (emu.conf.apiEnabled)
			ccComputer.addAPI(CCEmuXAPI(this, "ccemux"))

		ccComputer.turnOn()
	}

	fun getID() = ccComputer.id

	fun getLabel() = ccComputer.label

	fun addListener(l: Listener) = listeners.add(l)

	fun removeListener(l: Listener) = listeners.remove(l)

	fun isOn() = ccComputer.isOn

	fun turnOn() = ccComputer.turnOn()

	fun reboot() = ccComputer.reboot()

	fun shutdown() = ccComputer.shutdown()

	fun dispose() {
		ccComputer.shutdown()
		listeners.forEach { l -> l.onDispose() }
		emu.removeEmulatedComputer(this)
	}

	fun update(dt: Float) {
		ccComputer.advance(dt.toDouble())
		listeners.forEach { l -> l.onUpdate(dt) }
	}

	fun resize(width: Int, height: Int) {
		synchronized (terminal) {
			emu.conf.termWidth = width
			emu.conf.termHeight = height

			terminal.resize(width, height)
			listeners.forEach { l -> l.onTerminalResized(width, height) }
			ccComputer.queueEvent("term_resize", arrayOf(0))
		}
	}

	fun pressKey(keyCode: Int, release: Boolean) = ccComputer.queueEvent(if (release) "key_up" else "key", arrayOf(keyCode))

	fun pressChar(c: Char) = ccComputer.queueEvent("char", arrayOf(c.toString()))

	fun pasteText(text: String) = ccComputer.queueEvent("paste", arrayOf(text))

	fun terminateProgram() = ccComputer.queueEvent("terminate", arrayOf(0))

	fun click(button: Int, x: Int, y: Int, release: Boolean) {
		ccComputer.queueEvent(if (release) "mouse_up" else "mouse_click", arrayOf(button, x, y))
	}

	fun drag(button: Int, x: Int, y: Int) = ccComputer.queueEvent("mouse_drag", arrayOf(button, x, y))

	fun scroll(dir: Int, x: Int, y: Int) = ccComputer.queueEvent("mouse_scroll", arrayOf(dir, x, y))
}
