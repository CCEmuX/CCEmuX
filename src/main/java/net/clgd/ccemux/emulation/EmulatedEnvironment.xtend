package net.clgd.ccemux.emulation

import dan200.computercraft.ComputerCraft
import dan200.computercraft.core.computer.IComputerEnvironment
import dan200.computercraft.core.filesystem.FileMount
import java.io.File

class EmulatedEnvironment implements IComputerEnvironment {
	int nextID = 0
	
	override assignNewID() {
		return nextID++
	}
	
	override createResourceMount(String domain, String subPath) {
		return new ClasspathMount("/assets/" + domain + "/" + subPath)
	}
	
	override createSaveDirMount(String path, long capacity) {
		return new FileMount(new File(new File("saves"), path), capacity)
	}
	
	override getComputerSpaceLimit() {
		return Long.MAX_VALUE
	}
	
	override getDay() {
		return 0
	}
	
	override getHostString() {
		return "ComputerCraft " + ComputerCraft.version + " (CCEmuX v1.0)"
	}
	
	override getTimeOfDay() {
		return 0
	}
	
	override isColour() {
		return true
	}
	
}