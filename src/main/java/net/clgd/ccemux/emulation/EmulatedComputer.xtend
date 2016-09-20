package net.clgd.ccemux.emulation

import dan200.computercraft.core.computer.Computer
import dan200.computercraft.core.terminal.Terminal
import org.eclipse.xtend.lib.annotations.Accessors

class EmulatedComputer {	
	@Accessors(PUBLIC_GETTER) Terminal terminal
	@Accessors(PUBLIC_GETTER) Computer computer
	
	@Accessors char cursorChar = '_'
	
	new(int termWidth, int termHeight) {
		terminal = new Terminal(termWidth, termHeight)
		computer = new Computer(new EmulatedEnvironment(), terminal, 0)
		computer.addAPI(new CCEmuXAPI(this, "ccemux"))
		computer.turnOn()
	}
	
	def void update(float dt) {
		computer.advance(dt)
	}
}