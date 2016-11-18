package net.clgd.ccemux.emulation

import dan200.computercraft.ComputerCraft
import dan200.computercraft.core.computer.IComputerEnvironment
import dan200.computercraft.core.filesystem.ComboMount
import dan200.computercraft.core.filesystem.FileMount
import dan200.computercraft.core.filesystem.JarMount
import java.nio.file.Paths
import java.util.zip.ZipInputStream

class EmulatedEnvironment(val emu: CCEmuX) : IComputerEnvironment {
	private var nextID = 0

	override fun assignNewID() = nextID++

	override fun createResourceMount(domain: String, subPath: String): ComboMount {
		var path = Paths.get("assets", domain, subPath).toString().replace('\\', '/')

		if (path.startsWith('\\')) {
			path = path.substring(1)
		}

		return ComboMount(
			arrayOf(
				JarMount(emu.ccJar, path),
				CustomRomMount(
					ZipInputStream(EmulatedEnvironment::class.java.getResourceAsStream("/custom.rom"))
				)
			)
		)
	}

	override fun createSaveDirMount(path: String, capacity: Long): FileMount {
		return FileMount(emu.dataDir.resolve(path).toFile(), capacity)
	}

	override fun getComputerSpaceLimit() = Long.MAX_VALUE

	override fun getDay() = ((emu.getTicksSinceStart() + 6000L) / 24000L + 1).toInt()

	override fun getHostString() = "ComputerCraft %s (CCEmuX v%s)".format(ComputerCraft.getVersion(), CCEmuX.getVersion())

	override fun getTimeOfDay() = ((emu.getTicksSinceStart() + 6000) % 24000) / 1000.0

	override fun isColour() = true
}
