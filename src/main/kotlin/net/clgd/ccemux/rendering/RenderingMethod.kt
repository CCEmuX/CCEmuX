package net.clgd.ccemux.rendering

import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer

object RenderingMethod {
	fun create(type: String, emu: CCEmuX, computer: EmulatedComputer): Renderer? {
		synchronized(computer) {
			for (method in getMethods()) {
				if (method.name == type) {
					emu.logger.debug("Creating {} renderer", method.name);
					val renderer = method.create(emu, computer);
					computer.addListener(renderer);
					return renderer
				} else {
					return null
				}
			}

			emu.logger.error("Could not create renderer of type {}", type);
			throw IllegalArgumentException("Invalid renderer type " + type);
		}
	}

	fun getMethods() = RenderingMethods.values()
}
