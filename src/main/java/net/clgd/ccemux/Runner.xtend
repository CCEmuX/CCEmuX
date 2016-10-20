package net.clgd.ccemux

import java.awt.SplashScreen
import java.nio.file.Path
import java.util.HashMap
import java.util.List
import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.rendering.Renderer
import net.clgd.ccemux.rendering.RenderingMethod
import org.slf4j.Logger

class Runner {
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
