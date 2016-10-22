package net.clgd.ccemux.emulation

import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.core.apis.ILuaAPI
import java.awt.Desktop
import java.util.LinkedHashMap
import java.util.function.Function
import net.clgd.ccemux.rendering.RenderingMethod

class CCEmuXAPI implements ILuaAPI {
	String name
	EmulatedComputer computer

	// somewhat hacky but type inference doesn't cut it here
	val methods = new LinkedHashMap<String, Function<Object[], Object[]>> => [
		put("getVersion") [
			#[CCEmuX.version]
		]

		put("setCursorChar") [
			if (length < 1 || !(get(0) instanceof String)) {
				throw new LuaException("expected string for argument #1")
			}

			val cursorChar = get(0) as String

			if (cursorChar.length > 0) {
				computer.cursorChar = cursorChar.charAt(0)
			} else {
				throw new LuaException("cursor char can't be empty")
			}

			return #[]
		]

		put("setResolution") [
			if (length < 2 || !(get(0) instanceof Number) || !(get(1) instanceof Number)) {
				throw new LuaException("expected number for arguments #1 and #2")
			}

			var width = (get(0) as Number).intValue
			var height = (get(1) as Number).intValue

			if (width <= 0 || height <= 0) {
				throw new LuaException("width and height must be above 0")
			}

			computer.resize(width, height)

			return #[]
		]

		put("saveSettings") [
			computer.emu.conf.saveProperties

			return #[]
		]

		put("closeEmu") [
			computer.dispose

			return #[]
		]

		put("openEmu") [
			var id = -1

			if (size > 0 && get(0) != null)
				if (get(0) instanceof Number) {
					id = (get(0) as Number).intValue
				} else {
					throw new LuaException("expected number or nil for argument #1")
				}

			val ec = computer.emu.createEmulatedComputer(id)
			val r = RenderingMethod.create(computer.emu.conf.renderer, computer.emu, ec)

			r.visible = true

			return #[ec.ID]
		]

		put("openDataDir") [
			try {
				Desktop.desktop.browse(computer.emu.dataDir.toUri)

				return #[true]
			} catch (Exception e) {
				return #[false]
			}
		]

		put("milliTime")[#[System.currentTimeMillis]]
		put("nanoTime")[#[System.nanoTime]]

		put("echo") [
			if (size > 0 && get(0) != null && get(0) instanceof String)
				computer.emu.logger.info("[Computer {}] {}", computer.ID, get(0) as String)
			else
				throw new LuaException("expected string for argument #1")
			return #[]
		]
	]

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
		methods.getOrDefault(methods.keySet.get(method), [throw new LuaException("this shouldn't happen!")])
			.apply(arguments)
	}

	override getMethodNames() {
		methods.keySet
	}
}
