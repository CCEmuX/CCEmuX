package net.clgd.ccemux.emulation

import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.core.apis.ILuaAPI

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
				return newArrayList(CCEmuX.version)
			}	
			
			case 1: { // setCursorChar
				if (arguments.length < 1 || !(arguments.get(0) instanceof String)) {
					throw new LuaException("expected string for argument #1")
				}
				
				val cursorChar = arguments.get(0) as String
				
				if (cursorChar.length > 0) {
					computer.cursorChar = cursorChar.charAt(0)
				} else {
					throw new LuaException("cursor char can't be empty")
				}
				
				return newArrayOfSize(0)
			}
			
			case 2: { // close
				computer.dispose
				
				return null
			}
		}
	}
	
	override getMethodNames() {
		return newArrayList(
			"getVersion",
			"setCursorChar",
			"close"
		)
	}
}