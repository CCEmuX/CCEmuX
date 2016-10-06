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
import net.clgd.ccemux.emulation.CCEmuX
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static extension net.clgd.ccemux.Utils.*
import static extension org.apache.commons.codec.digest.DigestUtils.md5Hex
import static extension org.apache.commons.io.FileUtils.copyURLToFile

class Launcher {
	static val opts = new Options => [
		buildOpt("h") [
			longOpt("help")
			desc("Shows the help information")
		]

		buildOpt("d") [
			longOpt("data-dir")

			desc("Overrides the data directory.")
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

		buildOpt("C") [
			longOpt("skip-checksum")

			desc("If present, the CC jar checksum will not be validated.")
		]
	]

	private static Path dataDir
	private static Logger logger
	private static Config config
	private static CCEmuX emu

	// TODO: Split this up into smaller functions?
	def static void loadCC(boolean skipChecksum) {
		logger.debug("Bootstrapping CC")
		val jar = dataDir.resolve(config.CCLocal).toFile

		if (jar.exists && !skipChecksum) {
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

				if (!skipChecksum) {
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
			OperatingSystem.get.appDataDir

		logger.info("Data directory is {}", dataDir.toAbsolutePath.toString)

		logger.debug("Loading config")
		config = new Config(dataDir.resolve(Config.CONFIG_FILE_NAME).toFile);
		config.forEach [ name, value |
			logger.trace("-> {} = {}", name, value)
		]
		logger.info("Loaded configuration data")

		logger.trace("Loading CC")
		loadCC(cmd.hasOption("C"))
		try {
			logger.trace("Loaded CC version {}", ComputerCraft.version)
		} catch (Exception e) {
			logger.error("Failed to load CC!")
			logger.error(e.toString)
			System.exit(2)
		}

		emu = new CCEmuX(logger, config, dataDir, dataDir.resolve(config.CCLocal).toFile)

		val comp = emu.createEmulatedComputer
		val w = new EmulatorWindow(emu, comp)

		SplashScreen.splashScreen?.close

		w.visible = true
		emu.run
	}
}
