package net.clgd.ccemux.emulation

import dan200.computercraft.core.computer.Computer
import dan200.computercraft.core.terminal.Terminal
import java.util.ArrayList
import org.eclipse.xtend.lib.annotations.Accessors

class EmulatedComputer {	
	static interface Listener {
		def void onUpdate(float dt)
		def void onDispose()
	}
	
	@Accessors(PUBLIC_GETTER) val CCEmuX emu
	@Accessors(PUBLIC_GETTER) val Terminal terminal
	Computer ccComputer
	
	@Accessors char cursorChar = '_'
	
	val listeners = new ArrayList<Listener>()
	
	package new(CCEmuX emu, int termWidth, int termHeight) {
		this.emu = emu
		terminal = new Terminal(termWidth, termHeight)
		
		ccComputer = new Computer(emu.env, terminal, -1)
		ccComputer.assignID
		
		if (emu.conf.apiEnabled)
			ccComputer.addAPI(new CCEmuXAPI(this, "ccemux"))
		
		ccComputer.turnOn()
	}
	
	def getID() {
		ccComputer.ID
	}
	
	def addListener(Listener l) {
		return listeners.add(l)
	}
	
	def removeListener(Listener l) {
		return listeners.remove(l)
	}
	
	def isOn() {
		return ccComputer.on
	}
	
	def void turnOn() {
		ccComputer.turnOn
	}
	
	def void reboot() {
		ccComputer.reboot
	}
	
	def void shutdown() {
		ccComputer.shutdown
	}
	
	def void dispose() {
		ccComputer.shutdown
		
		listeners.forEach [
			onDispose()
		]
		
		emu.removeEmulatedComputer(this)
	}
	
	def void update(float dt) {
		ccComputer.advance(dt)
		
		listeners.forEach [
			onUpdate(dt)
		]
	}
	
	def void pressKey(int keyCode, boolean release) {
		ccComputer.queueEvent(if (release) "key_up" else "key", newArrayList(keyCode))
	}
	
	def void pressChar(char c) {
		ccComputer.queueEvent("char", newArrayList(c.toString))
	}
	
	def void pasteText(String text) {
		ccComputer.queueEvent("paste", newArrayList(text))
	}
	
	def void terminateProgram() {
		ccComputer.queueEvent("terminate", newArrayOfSize(0))
	}
	
	def void click(int button, int x, int y, boolean release) {
		ccComputer.queueEvent(if (release) "mouse_up" else "mouse_click", newArrayList(button, x, y))
	}
	
	def void drag(int button, int x, int y) {
		ccComputer.queueEvent("mouse_drag", newArrayList(button, x, y))
	}
	
	def void scroll(int dir, int x, int y) {
		ccComputer.queueEvent("mouse_scroll", newArrayList(dir, x, y))
	}
}