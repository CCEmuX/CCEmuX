package net.clgd.ccemux.emulation

import dan200.computercraft.ComputerCraft
import dan200.computercraft.core.computer.IComputerEnvironment
import dan200.computercraft.core.filesystem.FileMount
import dan200.computercraft.core.filesystem.JarMount
import java.io.File
import java.nio.file.Paths
import net.clgd.ccemux.CCBootstrapper
import net.clgd.ccemux.CCEmuX

class EmulatedEnvironment implements IComputerEnvironment {
	int nextID = 0
	
	override assignNewID() {
		return nextID++
	}
	
	override createResourceMount(String domain, String subPath) {
		var path = Paths.get("assets", domain, subPath).toString.replace('\\', '/')
		
		if (path.startsWith('\\'))
			path = path.substring(1)
		
		println(path)
		return new JarMount(CCBootstrapper.CCJar, path)
	}
	
	override createSaveDirMount(String path, long capacity) {
		return new FileMount(new File(new File("saves"), path), capacity)
	}
	
	override getComputerSpaceLimit() {
		return Long.MAX_VALUE
	}
	
	override getDay() {
		return ((CCEmuX.get.ticksSinceStart + 6000L) / 24000L + 1) as int
	}
	
	override getHostString() {
		return "ComputerCraft " + ComputerCraft.version + " (CCEmuX v1.0)"
	}
	
	override getTimeOfDay() {
		return ((CCEmuX.get.ticksSinceStart + 6000) % 24000) / 1000.0
	}
	
	override isColour() {
		return true
	}
	
}