package net.clgd.ccemux.emulation

import dan200.computercraft.core.computer.Computer
import dan200.computercraft.core.terminal.Terminal
import org.eclipse.xtend.lib.annotations.Accessors

class EmulatedComputer {	
	@Accessors(PUBLIC_GETTER) Terminal terminal
	Computer ccComputer
	
	@Accessors char cursorChar = '_'
	
	new(int termWidth, int termHeight) {
		terminal = new Terminal(termWidth, termHeight)
		ccComputer = new Computer(new EmulatedEnvironment(), terminal, 0)
		ccComputer.addAPI(new CCEmuXAPI(this, "ccemux"))
		ccComputer.turnOn()
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
	
	def void update(float dt) {
		ccComputer.advance(dt)
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