package net.clgd.ccemux

import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.rendering.BlankRenderer
import net.clgd.ccemux.rendering.Renderer
import net.clgd.ccemux.rendering.RenderingMethod
import org.slf4j.Logger
import org.squiddev.cctweaks.lua.ConfigPropertyLoader
import org.squiddev.cctweaks.lua.lib.ApiRegister
import java.awt.SplashScreen
import java.nio.file.Path
import java.util.*

object Runner {
	@JvmStatic fun launchCCTweaks(logger: Logger, config: Config, saveDirs: MutableList<Path>, count: Int) {
		// Add config listener to sync CCTweaks config.
		// This allows changing it at runtime, should you ever wish to do that.
		// This needs to be done in this class as the config class should be loaded within the
		// wrapped class loader.
		config.addListener(Runnable {
			config.entries.forEach {
				p -> run {
					val key = p.key

					if (key != null && key is String && key.startsWith("cctweaks")) {
						val value = p.value

						if (value != null && value is String) {
							System.setProperty(key, value)
						}
					}
				}
			}

			ConfigPropertyLoader.init()
		})

		// Redirect CCTweaks' logger
		org.squiddev.patcher.Logger.instance = object : org.squiddev.patcher.Logger() {
			override fun doDebug(message: String) = logger.debug(message)

			override fun doWarn(message: String) = logger.warn(message)

			override fun doError(message: String, e: Throwable) = logger.error(message, e)
		}

		// Various setup tasks for CCTweaks
		ApiRegister.init()

		// And launch!
		launch(logger, config, saveDirs, count)
	}

	@JvmStatic fun launch(logger: Logger, config: Config, saveDirs: MutableList<Path>, count: Int) {
		val emu = CCEmuX(logger, config)

		val computers = HashMap<EmulatedComputer, List<Renderer>>()

		for (i in 0..(count-1)) {
			val it = if (saveDirs.isNotEmpty()) {
				emu.createEmulatedComputer(saveDirs.removeAt(0))
			} else {
				emu.createEmulatedComputer()
			}

			computers.put(it, emu.conf.getRenderer().map { r -> RenderingMethod.create(r, emu, it) ?: BlankRenderer() })
		}

		SplashScreen.getSplashScreen()?.close()
		computers.forEach { ec, r -> r.forEach { f -> f.setVisible(true) } }
		emu.run()
	}
}
