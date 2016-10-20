package net.clgd.ccemux.emulation

import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.core.apis.ILuaAPI
import java.awt.Desktop
import net.clgd.ccemux.rendering.RenderingMethod

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
			case getMethodNames().indexOf("getVersion"): {
				return newArrayList(CCEmuX.version)
			}

			case getMethodNames().indexOf("setCursorChar"): {
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

			case getMethodNames().indexOf("setResolution"): {
				if (arguments.length < 2 || !(arguments.get(0) instanceof Number) || !(arguments.get(1) instanceof Number)) {
					throw new LuaException("expected number for arguments #1 and #2")
				}

				var width = (arguments.get(0) as Number).intValue
				var height = (arguments.get(1) as Number).intValue

				if (width <= 0 || height <= 0) {
					throw new LuaException("width and height must be above 0")
				}

				computer.resize(width, height)

				return newArrayOfSize(0)
			}

			case getMethodNames().indexOf("saveSettings"): {
				computer.emu.conf.saveProperties

				return newArrayOfSize(0)
			}

			case getMethodNames().indexOf("closeEmu"): {
				computer.dispose

				return null
			}

			case getMethodNames().indexOf("openEmu"): {
				var id = -1

				if (arguments.size > 0 && arguments.get(0) != null)
					if (arguments.get(0) instanceof Number) {
						id = (arguments.get(0) as Number).intValue
					} else {
						throw new LuaException("expected number or nil for argument #1")
					}

				val ec = computer.emu.createEmulatedComputer(id)
				val r = RenderingMethod.create(computer.emu.conf.renderer, computer.emu, ec)

				r.visible = true

				return # {ec.ID}
			}

			case getMethodNames().indexOf("openDataDir"): {
				try {
					Desktop.desktop.browse(computer.emu.dataDir.toUri)

					return # {true}
				} catch (Exception e) {
					return # {false}
				}
			}

			case getMethodNames().indexOf("milliTime"): return # { System.currentTimeMillis }

			case getMethodNames().indexOf("nanoTime"): return # { System.nanoTime }
		}
	}

	override getMethodNames() {
		return # {
			"getVersion",
			"setCursorChar",
			"setResolution",
			"closeEmu",
			"openEmu",
			"openDataDir",
			"milliTime",
			"nanoTime"
		}
	}
}