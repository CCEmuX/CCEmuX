package net.clgd.ccemux.emulation

import dan200.computercraft.ComputerCraft
import dan200.computercraft.core.computer.IComputerEnvironment
import dan200.computercraft.core.filesystem.ComboMount
import dan200.computercraft.core.filesystem.FileMount
import dan200.computercraft.core.filesystem.JarMount
import java.nio.file.Paths

class EmulatedEnvironment implements IComputerEnvironment {
	static val emuProgram = new CustomRomProgram("/programs/emu.lua", "emu")

	int nextID = 0
	final CCEmuX emu

	package new(CCEmuX emu) {
		this.emu = emu
	}

	override assignNewID() {
		return nextID++
	}

	override createResourceMount(String domain, String subPath) {
		var path = Paths.get("assets", domain, subPath).toString.replace('\\', '/')

		if (path.startsWith('\\'))
			path = path.substring(1)

		return new ComboMount(# {new JarMount(emu.ccJar, path), emuProgram})
	}

	override createSaveDirMount(String path, long capacity) {
		return new FileMount(emu.dataDir.resolve(path).toFile, capacity)
	}

	override getComputerSpaceLimit() {
		return Long.MAX_VALUE
	}

	override getDay() {
		return ((emu.ticksSinceStart + 6000L) / 24000L + 1) as int
	}

	override getHostString() {
		return "ComputerCraft " + ComputerCraft.version + " (CCEmuX v" + CCEmuX.version + ")"
	}

	override getTimeOfDay() {
		return ((emu.ticksSinceStart + 6000) % 24000) / 1000.0
	}

	override isColour() {
		return true
	}
}