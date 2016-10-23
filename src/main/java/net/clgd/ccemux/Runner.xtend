package net.clgd.ccemux

import java.awt.SplashScreen
import java.nio.file.Path
import java.util.Map
import java.util.HashMap
import java.util.List
import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.rendering.Renderer
import net.clgd.ccemux.rendering.RenderingMethod
import org.slf4j.Logger
import org.squiddev.cctweaks.lua.lib.ApiRegister
import org.squiddev.cctweaks.lua.ConfigPropertyLoader

class Runner {
	def static void launchCCTweaks(Logger logger, Config config, Path dataDir, List<Path> saveDirs, int count) {
		// Add config listener to sync CCTweaks config.
		// This allows changing it at runtime, should you ever wish to do that.
		// This needs to be done in this class as the config class should be loaded within the
		// wrapped class loader.
		config.addListener(new Runnable() {
			override void run() {
				for(Map.Entry<Object, Object> entry : config.entrySet()) {
					val key = entry.getKey() as String
					if(key.startsWith("cctweaks")) {
						System.setProperty(key, entry.getValue() as String)
					}
				}
				ConfigPropertyLoader.init
			}
		})

		// Redirect CCTweaks' logger
		org.squiddev.patcher.Logger.instance = new org.squiddev.patcher.Logger() {
			override void doDebug(String message) {
				logger.debug(message);
			}

			override void doWarn(String message) {
				logger.warn(message);
			}

			override void doError(String message, Throwable e) {
				logger.error(message, e);
			}
		}

		// Various setup tasks for CCTweaks
		ApiRegister.init()

		// And launch!
		launch(logger, config, dataDir, saveDirs, count);
	}

    def static void launch(Logger logger, Config config, Path dataDir, List<Path> saveDirs, int count) {
        val emu = new CCEmuX(logger, config, dataDir, dataDir.resolve(config.CCLocal).toFile)

        val computers = new HashMap<EmulatedComputer, Renderer>()

        for (i : 0 ..< count) {
            val it = if (saveDirs.size > 0) {
                emu.createEmulatedComputer(saveDirs.remove(0))
            } else {
                emu.createEmulatedComputer
            }
            computers.put(it, RenderingMethod.create(emu.conf.renderer, emu, it))
        }

        SplashScreen.splashScreen?.close

        computers.forEach [ ec, r |
            r.visible = true
        ]

        emu.run
    }
}
