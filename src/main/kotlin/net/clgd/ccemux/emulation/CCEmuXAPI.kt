package net.clgd.ccemux.emulation

import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.core.apis.ILuaAPI
import net.clgd.ccemux.rendering.RenderingMethod
import java.awt.Desktop
import java.util.*

class CCEmuXAPI(val computer: EmulatedComputer, val name: String) : ILuaAPI {
	// somewhat hacky but type inference doesn't cut it here
	val methods = LinkedHashMap<String, (Array<out Any>) -> Array<out Any>>()

	init {
		methods.put("getVersion") {
			arrayOf(CCEmuXConsts.getVersion())
		}

		methods.put("setCursorChar") {
			arr -> run {
				if (arr.isEmpty() || !(arr[0] is String)) {
					throw LuaException("expected string for argument #1")
				}

				val cursorChar = arr[0] as String

				if (cursorChar.isNotEmpty()) {
					computer.cursorChar = cursorChar.getOrElse(0, { i -> '?' })
				} else {
					throw LuaException("cursor char can't be empty")
				}

				emptyArray()
			}
		}

		methods.put("setResolution") {
			arr -> run {
				if (arr.size < 2 || !(arr.get(0) is java.lang.Number) || !(arr.get(1) is java.lang.Number)) {
					throw LuaException("expected number for arguments #1 and #2")
				}

				val width = (arr.get(0) as java.lang.Number).intValue()
				val height = (arr.get(1) as java.lang.Number).intValue()

				if (width <= 0 || height <= 0) {
					throw LuaException ("width and height must be above 0")
				}

				computer.resize(width, height)

				emptyArray()
			}
		}

		methods.put("saveSettings") {
			computer.emu.conf.saveProperties()
			emptyArray()
		}

		methods.put("closeEmu") {
			computer.dispose()
			emptyArray()
		}

		methods.put("openEmu") {
			arr -> run {
				var id = -1

				if (arr.isNotEmpty()) {
					val nid = arr[0]

					if (nid is java.lang.Number) {
						id = nid.intValue()
					} else {
						throw LuaException("expected number or nil for argument #1")
					}
				}

				val ec = computer.emu.createEmulatedComputer(id)
				val r = ec.emu.conf.getRenderer().map { r -> RenderingMethod.create(r, ec.emu, ec) }
				r.forEach({ b -> b?.setVisible(true) })

				arrayOf(ec.getID())
			}
		}

		methods.put("openDataDir") {
			try {
				Desktop.getDesktop().browse(computer.emu.dataDir.toUri())
				arrayOf(true)
			} catch (e: Exception) {
				arrayOf(false)
			}
		}

		methods.put("milliTime") { arrayOf(System.currentTimeMillis()) }
		methods.put("nanoTime") { arrayOf(System.nanoTime()) }

		methods.put("echo") {
			arr -> run {
				if (arr.isNotEmpty() && arr[0] is String) {
					computer.emu.logger.info("[Computer {}] {}", computer.getID(), arr[0] as String)
				} else {
					throw LuaException("expected string for argument #1")
				}

				emptyArray()
			}
		}
	}

	override fun advance(d: Double) {}

	override fun shutdown() {}

	override fun startup() {}

	override fun getNames() = arrayOf(name)

	override fun callMethod(lua: ILuaContext?, method: Int, arguments: Array<out Any>?): Array<out Any> {
		if (arguments != null) {
			return methods.getOrDefault(null, { i -> throw LuaException("this shouldn't happen!") })(arguments)
		} else {
			return arrayOf()
		}
	}

	override fun getMethodNames() = methods.keys.toTypedArray()
}
