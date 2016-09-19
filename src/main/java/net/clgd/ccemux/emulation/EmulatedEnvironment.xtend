package net.clgd.ccemux.emulation

import dan200.computercraft.ComputerCraft
import dan200.computercraft.core.computer.IComputerEnvironment
import dan200.computercraft.core.filesystem.FileMount
import java.io.File
import java.nio.file.Paths
import net.clgd.ccemux.CCEmuX

class EmulatedEnvironment implements IComputerEnvironment {
	int nextID = 0
	
	override assignNewID() {
		return nextID++
	}
	
	override createResourceMount(String domain, String subPath) {
		CCEmuX.get.logger.info("Creating resource mount @ domain " + domain + " in " + subPath)
		val path = Paths.get("/assets", domain, subPath).toString
		CCEmuX.get.logger.info("-> " + path)
		return new ClasspathMount((ComputerCraft), path)
	}
	
	override createSaveDirMount(String path, long capacity) {
		return new FileMount(new File(new File("saves"), path), capacity)
	}
	
	override getComputerSpaceLimit() {
		return Long.MAX_VALUE
	}
	
	override getDay() {
		return System.currentTimeMillis as int * 86400000
	}
	
	override getHostString() {
		return "ComputerCraft " + ComputerCraft.version + " (CCEmuX v1.0)"
	}
	
	override getTimeOfDay() {
		return System.currentTimeMillis as int
	}
	
	override isColour() {
		return true
	}
	
}