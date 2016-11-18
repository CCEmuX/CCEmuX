package net.clgd.ccemux.rendering.tror

import java.io.InputStream
import java.io.OutputStream
import java.util.HashMap
import java.util.function.BiConsumer
import java.util.regex.MatchResult
import java.util.regex.Pattern
import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.emulation.tror.CapabilitiesPacket
import net.clgd.ccemux.emulation.tror.ConnectionClosedPacket
import net.clgd.ccemux.emulation.tror.TRoRPacket
import net.clgd.ccemux.emulation.tror.TRoRTerminal
import net.clgd.ccemux.rendering.Renderer

import static extension java.lang.Integer.parseInt

class TRoRRenderer implements Renderer {
	static val Pattern eventPattern = Pattern.compile("^EV:([^;]*);(.*)$")
	
	static val handlers = new HashMap<Pattern, BiConsumer<EmulatedComputer, MatchResult>> => [
		// char
		put(Pattern.compile("^\"char\",\"(.)\""), [ec, m|
			ec.pressChar(m.group(1).charAt(0))
		])
		
		// key
		put(Pattern.compile("^\"key\",(\\d+)"), [ec, m|
			ec.pressKey(m.group(1).parseInt, false)
		])
		
		// key_up
		put(Pattern.compile("^\"key_up\",(\\d+)"), [ec, m|
			ec.pressKey(m.group(1).parseInt, true)
		])
		
		// paste
		put(Pattern.compile("^\"paste\",\"(.+)\""), [ec, m|
			ec.pasteText(m.group(1))
		])
	]
	
	static def matches(String s, Pattern p) {
		val m = p.matcher(s)
		if (m.matches) {
			return m.toMatchResult
		} else {
			null
		}
	}
	
	val CCEmuX emu
	val EmulatedComputer ec
	val String id
	val TRoRTerminal term
	val OutputStream output
	val AsyncScanner input

	var firstsend = true
	var paused = true
	var prefix = ""

	public new(CCEmuX emu, EmulatedComputer ec, OutputStream output, InputStream input) {
		this.emu = emu
		this.output = output
		this.input = new AsyncScanner(input)
		this.ec = ec
		id = ec.ID.toString
		
		if (ec.terminal instanceof TRoRTerminal) {
			term = ec.terminal as TRoRTerminal
		} else {
			term = null
			throw new IllegalArgumentException("Cannot create TRoR renderer unless TRoR terminal is used")
		}
		
		this.input.start
	}

	public new(CCEmuX emu, EmulatedComputer ec) {
		this(emu, ec, System.out, System.in)
		prefix = "[TRoR]"
	}

	override isVisible() {
		!paused
	}

	override setVisible(boolean visible) {
		paused = !visible
	}

	override resize(int width, int height) {
	}

	private def send(TRoRPacket<? extends Object> packet) {
		output.write((prefix + packet.toString(id)).bytes)
	}

	override onUpdate(float dt) {
		if (!paused) {
			if (firstsend) {
				firstsend = false
				send(new CapabilitiesPacket(#{"ccemux"}))
			}
			
			term.popQueue.forEach [
				send(it)
			]
			
			while (input.hasLines) {
				input.lines.forEach[
					val m = it.matches(eventPattern)
					if (m?.group(1) == id) {
						handlers.forEach[p, f|
							val m2 = m.group(2).matches(p)
							if (m2 != null) {
								f.accept(ec, m2)
							}
						]
					}
				]
			}
		}
	}

	override onDispose() {
		send(new ConnectionClosedPacket)
	}

	override onTerminalResized(int width, int height) {
	}

}
