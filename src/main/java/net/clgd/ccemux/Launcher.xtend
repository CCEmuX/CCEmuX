package net.clgd.ccemux

import dan200.computercraft.ComputerCraft
import java.awt.SplashScreen
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.HashMap
import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.rendering.Renderer
import net.clgd.ccemux.rendering.RenderingMethod
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static extension net.clgd.ccemux.Utils.*
import static extension org.apache.commons.codec.digest.DigestUtils.md5Hex
import static extension org.apache.commons.io.FileUtils.copyURLToFile
import java.util.ArrayList

class Launcher {
	static val opts = new Options => [
		buildOpt("h") [
			longOpt("help")
			desc("Shows the help information")
		]

		buildOpt("d") [
			longOpt("data-dir")

			desc("Overrides the data directory, where the CC jar, config, and default save directory are stored")
			hasArg
			optionalArg(true)
			argName("path")
		]

		buildOpt("l") [
			longOpt("log-level")

			desc(
				"Manually specify the logging level. Valid options are 'trace', 'debug', 'info', 'warning', and 'error'.")
			hasArg
			argName("level")
		]

		buildOpt("r") [
			longOpt("renderer")

			desc("Sets the renderer to use, run without an argument to show available renderers")
			hasArg
			optionalArg(true)
			argName("type")
		]
		
		buildOpt("c") [
			longOpt("count")
			
			desc("How many emulated computers to create")
			hasArg
			argName("amount")
		]
		
		buildOpt("s") [
			longOpt("save-dir")
			
			desc("Overrides the save directory where CC computers save their files, separated by commas. The first value is used for the first computer (ID 0), the second for the second (ID 1), etc.")
			hasArg
			argName("paths")
		]
	]

	private static Path dataDir
	private static Logger logger
	private static Config config
	private static CCEmuX emu

	// TODO: Split this up into smaller functions?
	def static void loadCC() {
		logger.debug("Bootstrapping CC")
		val jar = dataDir.resolve(config.CCLocal).toFile

		if (jar.exists) {
			logger.trace("CC Checksum is '{}'", new FileInputStream(jar).with[md5Hex])

			if (config.CCChecksum.nullOrEmpty) {
				logger.warn("Skipping jar validation - checksum not specified.")
			} else {
				val checksum = new FileInputStream(jar).with[md5Hex]
				logger.debug("Expected checksum is '{}'", config.CCChecksum)

				if (checksum != config.CCChecksum) {
					if (config.CCRemote.nullOrEmpty) {
						logger.warn("CC jar validation failed! Errors may occur!")
					} else {
						logger.warn("CC jar validation failed! Attempting redownload")
						logger.trace("Deletion: {}", jar.delete)
					}
				}
			}
		}

		if (!jar.exists) {
			if (config.CCRemote.nullOrEmpty) {
				logger.error("CC jar not present and no download available, save CC jar to {}", jar.absolutePath)
				throw new FileNotFoundException("CC jar missing")
			} else {
				logger.info("Downloading CC jar")
				new URL(config.CCRemote).copyURLToFile(jar, 5000, 5000)
				logger.debug("Download complete")

				logger.trace("CC Checksum is '{}'", new FileInputStream(jar).with[md5Hex])

				if (config.CCChecksum.nullOrEmpty) {
					logger.warn("Skipping jar validation - checksum not specified")
				} else {
					val checksum = new FileInputStream(jar).with[md5Hex]
					logger.debug("Expected checksum is '{}'", config.CCChecksum)

					if (checksum != config.CCChecksum) {
						logger.error("CC jar validation failed!")
						throw new IllegalStateException("CC jar validation failed")
					}
				}
			}
		}

		logger.debug("Loading CC into classpath")
		val loader = ClassLoader.systemClassLoader as URLClassLoader
		val m = URLClassLoader.getDeclaredMethod("addURL", (URL))
		m.accessible = true
		m.invoke(loader, jar.toURI.toURL)
	}

	def static void main(String[] args) {
		val cmd = new DefaultParser().parse(opts, args)

		if (cmd.hasOption('h')) {
			new HelpFormatter().printHelp(
				"java -jar " + new File(Launcher.getProtectionDomain.codeSource.location.toURI).name + " <args>", opts)
			System.exit(1)
		}

		if (cmd.hasOption('r') && cmd.getOptionValue('r').nullOrEmpty) {
			System.out.format("Available rendering methods: %s\n", RenderingMethod.methods.map[name].reduce [p1, p2|
				p1 + ", " + p2
			])
			System.exit(1)
		}

		(cmd.getOptionValue('l') ?: "info").trim => [
			if (#{"trace", "debug", "info", "warning", "error"}.contains(it))
				System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", it)
			else
				System.err.format("Invalid logging level '%s'\n", it)
		]

		logger = LoggerFactory.getLogger("CCEmuX")
		logger.info("Starting CCEmuX")

		dataDir = if (cmd.hasOption('d'))
			Paths.get(cmd.getOptionValue('d') ?: "")
		else
			OperatingSystem.get.appDataDir.resolve("ccemux")

		dataDir.toFile.mkdirs
		logger.info("Data directory is {}", dataDir.toAbsolutePath.toString)

		logger.debug("Loading config")
		config = new Config(dataDir.resolve(Config.CONFIG_FILE_NAME).toFile);
		config.forEach [ name, value |
			logger.trace("-> {} = {}", name, value)
		]
		logger.info("Loaded configuration data")

		logger.trace("Loading CC")
		loadCC()
		try {
			logger.trace("Loaded CC version {}", ComputerCraft.version)
		} catch (Exception e) {
			logger.error("Failed to load CC!")
			logger.error(e.toString)
			System.exit(2)
		}

		if (cmd.hasOption("r")) {
			config.setProperty("renderer", cmd.getOptionValue("r").trim)
		}

		emu = new CCEmuX(logger, config, dataDir, dataDir.resolve(config.CCLocal).toFile)

		val computers = new HashMap<EmulatedComputer, Renderer>()

		val count = try {
			Integer.parseInt(cmd.getOptionValue("c", "1"))
		} catch (NumberFormatException e) {
			logger.error("Invalid computer count", e)
			System.exit(3)
			0 // satisfy variable
		}
		
		val saveDirs = if (cmd.hasOption("s")) {
			new ArrayList(cmd.getOptionValue("s", "").split(',').map[Paths.get(it).toAbsolutePath])
		} else {
			new ArrayList()
		}
		
		
		if (count < 1) {
			logger.error("Cannot create fewer than 1 computer")
			System.exit(3)
		}

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
		
		logger.info("Exiting CCEmuX")
		System.exit(0)
	}
}
