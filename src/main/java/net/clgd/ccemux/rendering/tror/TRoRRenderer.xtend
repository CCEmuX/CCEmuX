package net.clgd.ccemux.rendering.tror

import java.io.InputStream
import java.io.OutputStream
import java.util.Scanner
import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.emulation.tror.CapabilitiesPacket
import net.clgd.ccemux.emulation.tror.ConnectionClosedPacket
import net.clgd.ccemux.emulation.tror.TRoRPacket
import net.clgd.ccemux.emulation.tror.TRoRTerminal
import net.clgd.ccemux.rendering.Renderer

class TRoRRenderer implements Renderer {
	val CCEmuX emu
	val EmulatedComputer ec
	val String id
	val TRoRTerminal term
	val OutputStream output
	val Scanner input
	
	var firstsend = true
	var paused = true
	var prefix = ""
	
	public new(CCEmuX emu, EmulatedComputer ec, OutputStream output, InputStream input) {
		this.emu = emu
		this.output = output
		this.input = new Scanner(input)
		this.ec = ec
		id = ec.ID.toString
		if (ec.terminal instanceof TRoRTerminal) {
			term = ec.terminal as TRoRTerminal
		} else {
			term = null
			throw new IllegalArgumentException("")
		}
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
			term.popQueue.forEach[
				send(it)
			]
		}
	}
	
	override onDispose() {
		send(new ConnectionClosedPacket)
	}
	
	override onTerminalResized(int width, int height) {
		
	}
	
}