package net.clgd.ccemux.emulation

import dan200.computercraft.core.computer.Computer
import dan200.computercraft.core.filesystem.FileMount
import java.io.File
import java.nio.file.Path
import java.util.ArrayList
import net.clgd.ccemux.Config
import org.eclipse.xtend.lib.annotations.Accessors
import org.slf4j.Logger

class CCEmuX implements Runnable {
	def static getVersion() {
		return CCEmuX.package?.implementationVersion ?: "[Unknown]"
	}

	@Accessors(PUBLIC_GETTER) final Logger logger
	@Accessors(PUBLIC_GETTER) final Config conf
	@Accessors(PUBLIC_GETTER) final Path dataDir
	@Accessors(PUBLIC_GETTER) final File ccJar

	@Accessors(PUBLIC_GETTER) final EmulatedEnvironment env

	@Accessors(PUBLIC_GETTER) boolean running
	@Accessors(PUBLIC_GETTER) long timeStarted

	@Accessors(PUBLIC_GETTER) val computers = new ArrayList<EmulatedComputer>()

	new(Logger logger, Config conf, Path dataDir, File ccJar) {
		this.logger = logger
		this.conf = conf
		this.dataDir = dataDir
		this.ccJar = ccJar

		env = new EmulatedEnvironment(this)
	}

	def createEmulatedComputer(Path saveDir) {
		createEmulatedComputer => [
			logger.debug("Overriding save dir for computer {} to '{}'", ID, saveDir.toString)
			val field = Computer.getDeclaredField("m_rootMount")
			field.accessible = true
			field.set(ccComputer, new FileMount(saveDir.toFile, env.computerSpaceLimit))
		]
	}

	def createEmulatedComputer(int id) {
		logger.trace("Creating emulated computer")
		synchronized(computers) {
			return new EmulatedComputer(this, conf.termWidth, conf.termHeight, id) => [
				computers.add(it)
				logger.info("Created emulated computer ID {}", ID)
			]
		}
	}

	def createEmulatedComputer() {
		createEmulatedComputer(-1)
	}

	package def removeEmulatedComputer(EmulatedComputer ec) {
		synchronized(computers) {
			if (computers.contains(ec)) {
				logger.trace("Removing emulated computer ID {}", ec.ID)
				val success = computers.remove(ec)

				if (computers.empty) {
					running = false
					logger.info("All emulated computers removed, stopping event loop")
				}

				return success
			} else {
				return false
			}
		}
	}

	def getTimeStartedInSeconds() {
		return timeStarted / 1000.0f
	}

	def getTimeStartedInTicks() {
		return timeStartedInSeconds * 20.0f
	}

	def getTimeSinceStart() {
		return System.currentTimeMillis - timeStarted
	}

	def getSecondsSinceStart() {
		return timeSinceStart / 1000.0f
	}

	def getTicksSinceStart() {
		return (secondsSinceStart * 20) as int
	}

	def static getGlobalCursorBlink() {
		return System.currentTimeMillis / 400 % 2 == 0
	}

	private def update(float dt) {
		synchronized(computers) {
			computers.forEach [
				synchronized(it)
					it.update(dt)
			]
		}
	}

	override run() {
		running = true

		timeStarted = System.currentTimeMillis
		var lastTime = System.currentTimeMillis

		while (running) {
			val now = System.currentTimeMillis
			val dt = now - lastTime
			val dtSecs = dt / 1000.0f

			update(dtSecs)

			lastTime = System.currentTimeMillis

			// ComputerCraft only needs to update 20 times a second.
			Thread.sleep(1000 / 20)
		}

		logger.debug("Emulation stopped")
	}
}
		