package net.clgd.ccemux.emulation

import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.core.apis.ILuaAPI
import org.luaj.vm2.LuaError

class CCEmuXAPI implements ILuaAPI {
	String name
	EmulatedComputer computer
	
	new(EmulatedComputer computer, String name) {
		this.name = name	
		this.computer = computer
	}
	
	override advance(double d) {

	}
	
	override shutdown() {
		
	}
	
	override startup() {
		
	}
	
	override getNames() {
		return newArrayList(name)
	}
	
	override callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0: { // getVersion
				return newArrayList("1.0")
			}	
			
			case 1: { // setCursorChar
				if (arguments.length < 1 || !(arguments.get(0) instanceof String)) {
					throw new LuaError("expected string for argument #1")
				}
				
				computer.cursorChar = (arguments.get(0) as String).charAt(0)
				return newArrayOfSize(0)
			}
		}
	}
	
	override getMethodNames() {
		return newArrayList(
			"getVersion",
			"setCursorChar"
		)
	}
	
}