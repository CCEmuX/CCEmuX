package net.clgd.ccemux

import dan200.computercraft.ComputerCraft
import net.clgd.ccemux.Utils.buildOpt
import net.clgd.ccemux.Utils.getMD5Checksum
import net.clgd.ccemux.emulation.OperatingSystem
import net.clgd.ccemux.rendering.RenderingMethod
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.SplashScreen
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JOptionPane

object Launcher {
	val opts = object : Options() {
		init {
			buildOpt("h") {
				b -> b.longOpt("help")
					.desc("Shows the help information")
			}

			buildOpt("d") {
				b -> b.longOpt("data-dir")
					.desc("Overrides the data directory, where the CC jar, config, and default save directory are stored")
					.hasArg()
					.optionalArg(true)
					.argName("path")
			}

			buildOpt("l") {
				b -> b.longOpt("log-level")
					.desc("Manually specify the logging level. Valid options are 'trace', 'debug', 'info', 'warning', and 'error'.")
					.hasArg()
					.argName("level")
			}

			buildOpt("r") {
				b -> b.longOpt("renderer")
					.desc("Sets the renderer to use, run without an argument to show available renderers")
					.hasArg()
					.optionalArg(true)
					.argName("type")
			}

			buildOpt("c") {
				b -> b.longOpt("count")
					.desc("How many emulated computers to create")
					.hasArg()
					.argName("amount")
			}

			buildOpt("s") {
				b -> b.longOpt("save-dir")
					.desc("Overrides the save directory where CC computers save their files, separated by commas. The first value is used for the first computer (ID 0), the second for the second (ID 1), etc.")
					.hasArg()
					.argName("paths")
			}
		}
	}

	var dataDir: Path? = null
	var logger: Logger? = null
	var config: Config? = null

	// TODO: Split this up into smaller functions?
	fun loadCC() {
		logger?.debug("Bootstrapping CC")
		val jar = dataDir?.resolve(config?.getCCLocal())?.toFile()

		if (jar?.exists() ?: false) {
			val checksum = jar?.getMD5Checksum() ?: "Unknown"
			logger?.trace("CC Checksum is '{}'", checksum)

			if (config?.getCCChecksum().isNullOrEmpty()) {
				logger?.warn("Skipping jar validation - checksum not specified.")
			} else {
				logger?.debug("Expected checksum is '{}'", config?.getCCChecksum() ?: "Unknown")

				if (checksum != config?.getCCChecksum()) {
					if (config?.getCCRemote().isNullOrEmpty()) {
						logger?.warn("CC jar validation failed! Errors may occur!")
					} else {
						logger?.warn("CC jar validation failed! Attempting redownload")
						logger?.trace("Deletion: {}", jar?.delete())
					}
				}
			}
		} else {
			if (config?.getCCRemote().isNullOrEmpty()) {
				logger?.error("CC jar not present and no download available, save CC jar to {}", jar?.absolutePath ?: "Unknown")
				throw FileNotFoundException("CC jar missing")
			} else {
				logger?.info("Downloading CC jar")
				FileUtils.copyURLToFile(URL(config?.getCCRemote()), jar, 5000, 5000)
				logger?.debug("Download complete")

				val checksum = jar?.getMD5Checksum() ?: "Unknown"
				logger?.trace("CC Checksum is '{}'", checksum)

				if (config?.getCCChecksum().isNullOrEmpty()) {
					logger?.warn("Skipping jar validation - checksum not specified")
				} else {
					logger?.debug("Expected checksum is '{}'", config?.getCCChecksum() ?: "Unknown")

					if (checksum != config?.getCCChecksum()) {
						logger?.error("CC jar validation failed!")
						throw IllegalStateException("CC jar validation failed")
					}
				}
			}
		}

		logger?.debug("Loading CC into classpath")
		val loader = ClassLoader.getSystemClassLoader()

		if (loader is URLClassLoader) {
			val m = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
			m.isAccessible = true
			m.invoke(loader, jar?.toURI()?.toURL())
		}
	}

	@JvmStatic fun main(args: Array<String>): Unit {
		try {
			val cmd = DefaultParser().parse(Launcher.opts, args)

			if (cmd.hasOption('h')) {
				HelpFormatter().printHelp(
						"java -jar " + File(Launcher::class.java.getProtectionDomain().codeSource.location.toURI()).name + " <args>",
						Launcher.opts
				)
				System.exit(1)
			}

			if (cmd.hasOption('r') && cmd.getOptionValue('r').isNullOrEmpty()) {
				System.out.format("Available rendering methods: %s\n",
						RenderingMethod.getMethods().map { m -> m.name }.reduce { p1, p2 -> p1 + ", " + p2 })
				System.exit(1)
			}

			val logLevel = (cmd.getOptionValue('l') ?: "info").trim()

			if (listOf("trace", "debug", "info", "warning", "error").contains(logLevel)) {
				System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", logLevel)
			} else {
				System.err.format("Invalid logging level '%s'\n", logLevel)
			}

			Launcher.logger = LoggerFactory.getLogger("CCEmuX")
			Launcher.logger?.info("Starting CCEmuX")

			Launcher.dataDir = if (cmd.hasOption('d')) {
				Paths.get(cmd.getOptionValue('d') ?: "")
			} else {
				OperatingSystem.get().appDataDir.resolve("ccemux")
			}

			Launcher.dataDir?.toFile()?.mkdirs()
			Launcher.logger?.info("Data directory is {}", Launcher.dataDir?.toAbsolutePath()?.toString())

			Launcher.logger?.debug("Loading config")
			Launcher.config = Config(Launcher.dataDir ?: Paths.get("./crap"));
			Launcher.config?.forEach { name, value -> Launcher.logger?.trace("-> {} = {}", name, value) }
			Launcher.logger?.info("Loaded configuration data")

			Launcher.logger?.trace("Loading CC")
			Launcher.loadCC()
			try {
				Launcher.logger?.trace("Loaded CC version {}", ComputerCraft.getVersion())
			} catch (e: Exception) {
				Launcher.logger?.error("Failed to load CC!")
				Launcher.logger?.error(e.toString())
				System.exit(2)
			}

			if (cmd.hasOption("r")) {
				Launcher.config?.setProperty("renderer", cmd.getOptionValue("r").trim())
			}

			val count = try {
				Integer.parseInt(cmd.getOptionValue("c", "1"))
			} catch (e: NumberFormatException) {
				Launcher.logger?.error("Invalid computer count", e)
				System.exit(3)
				0 // satisfy variable
			}

			val saveDirs = if (cmd.hasOption("s")) {
				listOf(cmd.getOptionValue("s", "").split(',').map { s -> Paths.get(s).toAbsolutePath() })
			} else {
				listOf()
			}


			if (count < 1) {
				Launcher.logger?.error("Cannot create fewer than 1 computer")
				System.exit(3)
			}

			val mainLoader: ClassLoader
			val mainMethod: String

			if (Launcher.config?.getCCTweaks() ?: false) {
				Launcher.logger?.info("Injecting CCTweaks classloader")

				val loader = org.squiddev.cctweaks.lua.launch.Launcher.setupLoader();
				loader.addClassLoaderExclusion("net.clgd.ccemux.Config")
				loader.addClassLoaderExclusion("net.clgd.ccemux.Launcher")
				loader.addClassLoaderExclusion("org.slf4j.")
				loader.addClassLoaderExclusion("javax.")
				loader.addClassLoaderExclusion("org.apache.")

				loader.chain?.finalise()

				mainMethod = "launchCCTweaks"
				mainLoader = loader
			} else {
				mainMethod = "launch"
				mainLoader = ClassLoader.getSystemClassLoader()
			}

			val method = mainLoader.loadClass("net.clgd.ccemux.Runner")
				.getMethod(mainMethod, Logger::class.java, Config::class.java, MutableList::class.java, Int::class.java)

			val logger = Launcher.logger
			val config = Launcher.config

			if (method != null && logger != null && config != null) {
				method.invoke(null, logger, config, saveDirs, count)
			} else {
				Launcher.logger?.error("Main method was null")
			}

			Launcher.logger?.info("Exiting CCEmuX")
			System.exit(0)
		} catch (e: Exception) {
			try {
				var message = "CCEmuX has crashed! \n\n" + e.toString()
				var e2 = e.cause

				while (true) {
					e2 = e2?.cause ?: break
					message += '\n' + e2.toString()
				}

				message += "\n\nCheck console for more details. If this continues, please create a bug report."

				e.printStackTrace()
				System.err.println("Uncaught exception!")
				SplashScreen.getSplashScreen()?.close()
				JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE)
			} catch (e2: Exception) {
			} finally {
				System.exit(-1)
			}
		}
	}
}
