package net.clgd.ccemux.rendering.tror

import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.emulation.tror.CapabilitiesPacket
import net.clgd.ccemux.emulation.tror.ConnectionClosedPacket
import net.clgd.ccemux.emulation.tror.TRoRPacket
import net.clgd.ccemux.emulation.tror.TRoRTerminal
import net.clgd.ccemux.rendering.Renderer
import java.io.InputStream
import java.io.OutputStream

class TRoRRenderer(val emu: CCEmuX, val ec: EmulatedComputer, private val outputStream: OutputStream, inputStream: InputStream) : Renderer {

	val id = ec.getID().toString()
	val term = if (ec.terminal is TRoRTerminal) {
		ec.terminal
	} else {
		throw IllegalArgumentException("Cannot create TRoR renderer unless a TRoR terminal is used")
	}

	var firstsend = true
	var paused = true
	var prefix = ""
	
	constructor(emu: CCEmuX, ec: EmulatedComputer) : this(emu, ec, System.out, System.`in`) {
		prefix = "[TRoR]"
	}

	override fun isVisible() = !paused

	override fun setVisible(visible: Boolean) {
		paused = !visible
	}

	override fun resize(width: Int, height: Int) {
	}

	fun send(packet: TRoRPacket<*>) {
		outputStream.write((prefix + packet.toString(id)).toByteArray(Charsets.UTF_8))
	}

	override fun onUpdate(dt: Float) {
		if (!paused) {
			if (firstsend) {
				firstsend = false
				send(CapabilitiesPacket(setOf("ccemux")))
			}

			term.popQueue().forEach { t -> send(t) }
		}
	}

	override fun onDispose() {
		send(ConnectionClosedPacket())
	}

	override fun onTerminalResized(width: Int, height: Int) {
	}
}
