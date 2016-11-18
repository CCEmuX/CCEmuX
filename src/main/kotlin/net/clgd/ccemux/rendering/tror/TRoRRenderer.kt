package net.clgd.ccemux.rendering.tror

import net.clgd.ccemux.Utils.parseInt
import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.emulation.tror.CapabilitiesPacket
import net.clgd.ccemux.emulation.tror.ConnectionClosedPacket
import net.clgd.ccemux.emulation.tror.TRoRPacket
import net.clgd.ccemux.emulation.tror.TRoRTerminal
import net.clgd.ccemux.rendering.Renderer
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class TRoRRenderer(val emu: CCEmuX, val ec: EmulatedComputer, private val outputStream: OutputStream, inputStream: InputStream) : Renderer {
	companion object {
		val eventPattern = Pattern.compile("^EV:([^;]*);(.*)$")

		val handlers = HashMap<Pattern, (EmulatedComputer, Matcher) -> Unit>()

		init {
			// char
			handlers.put(
					Pattern.compile("^\"char\",\"(.)\""),
					{ ec, m -> ec.pressChar(m.group(1).getOrElse(0, { ' ' })) }
			)

			// key
			handlers.put(
					Pattern.compile("^\"key\",(\\d+)"),
					{ ec, m -> ec.pressKey(m.group(1).parseInt(), false) }
			)

			// key_up
			handlers.put(
					Pattern.compile("^\"key_up\",(\\d+)"),
					{ ec, m -> ec.pressKey(m.group(1).parseInt(), true) }
			)

			// paste
			handlers.put(
					Pattern.compile("^\"paste\",\"(.+)\""),
					{ ec, m -> ec.pasteText(m.group(1)) }
			)
		}
	}

	val id = ec.getID().toString()
	val term = if (ec.terminal is TRoRTerminal) {
		ec.terminal
	} else {
		throw IllegalArgumentException("Cannot create TRoR renderer unless a TRoR terminal is used")
	}

	val input = AsyncScanner(inputStream)

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

			while (input.hasLines()) {
				input.lines.forEach {
					it -> run {
						val m = TRoRRenderer.eventPattern.matcher(it)
						if (m?.group(1) == id) {
							handlers.forEach { p, f ->
								val m2 = TRoRRenderer.eventPattern.matcher(m.group(2))
								if (m2 != null) {
									f(ec, m2)
								}
							}
						}
					}
				}
			}
		}
	}

	override fun onDispose() {
		send(ConnectionClosedPacket())
	}

	override fun onTerminalResized(width: Int, height: Int) {
	}
}
