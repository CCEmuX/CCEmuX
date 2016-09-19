package net.clgd.ccemux

import dan200.computercraft.ComputerCraft
import java.awt.SplashScreen
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.eclipse.xtend.lib.annotations.Accessors
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static extension net.clgd.ccemux.Utils.*

class CCEmuX implements Runnable {
	static val opts = new Options().using [
		buildOpt("h") [
			longOpt("help")
			desc("Shows the help information")
		]

		buildOpt("p") [
			longOpt("portable")

			desc("Forces portable mode, in which all files (configs, saves, libraries) are kept in the same folder as CCEmuX." +
				"Will automatically be enabled if the config file is in the same folder as CCEmuX.")
		]
		
		buildOpt("d") [
			longOpt("data-dir")
			
			desc("Manually sets the data directory. Overrides -p/--portable.")
			hasArg()
			argName("path")
		]
		
		buildOpt("l") [
			longOpt("log-level")
			
			desc("Manually specify the logging level. Valid options are 'trace', 'debug', 'info', 'warning', and 'error', in that order from most verbose to least.")
			hasArg()
			argName("level")
		]
	]
	
	@Accessors(PUBLIC_GETTER) Logger logger
	@Accessors(PUBLIC_GETTER) EmulatorWindow window
	@Accessors(PUBLIC_GETTER) Config conf
	@Accessors(PUBLIC_GETTER) var portable = false
	@Accessors(PUBLIC_GETTER) Path dataDir
	@Accessors boolean running

	static CCEmuX instance

	static def get() {
		return instance
	}
	
	def static void printHelp() {
		val hf = new HelpFormatter
		hf.printHelp("java -jar " + new File(CCEmuX.getProtectionDomain.codeSource.location.toURI).name + " <args>", opts)
	}
	
	def static void main(String[] args) {
		instance = new CCEmuX(args)
		instance.bootstrap
		
		SplashScreen.splashScreen?.close
		
		instance.run()
	}

	new(String... args) {
		val cmd = new DefaultParser().parse(opts, args)

		if (cmd.hasOption('h')) {
			printHelp()
			System.exit(1)
		}

		portable = cmd.hasOption('p')
		
		cmd.getOptionValue("l")?.trim.using [
			if (#{"trace", "debug", "info", "warning", "error"}.contains(it))
				System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", it)
			else
				System.err.println("Invalid logging level: " + it)
		]
		
		logger = LoggerFactory.getLogger("CCEmuX")
		logger.info("Starting CCEmuX...")
		
		dataDir = if(cmd.hasOption('d')) Paths.get(cmd.getOptionValue('d')) else if(portable) Paths.get("") else OperatingSystem.get.appDataDir.resolve("ccemux")
		logger.debug("Data directory is {}", dataDir.toAbsolutePath.toString)
		Files.createDirectories(dataDir)
		
		logger.debug("Loading configuration data...", dataDir.resolve(Config.CONFIG_FILE_NAME).toString)
		conf = new Config(dataDir.resolve(Config.CONFIG_FILE_NAME).toFile)
		logger.debug("Configuration data loaded.")
	}
	
	def bootstrap() {
		CCBootstrapper.loadCC(logger)
		logger.info("Loaded CC (v{})", ComputerCraft.version)
		
		if (ComputerCraft.version != conf.CCRevision) {
			logger.warn(
				"Potential compatibility issues detected - expected CC version (v{}) does not match loaded CC version (v{})",
				conf.CCRevision,
				ComputerCraft.version
			)
		}
	}
	
	private def update(float dt) {
		window.update(dt)
	}
	
	private def startLoop() {
		running = true
		
		val started = System.currentTimeMillis
		var lastTime = started
		
		while (running) {
			val now = System.currentTimeMillis
			val dt = now - lastTime
			val dtSecs = dt / 1000.0f
			
			logger.trace("\u0394t = " + dtSecs)
			update(dtSecs)
			
			lastTime = System.currentTimeMillis	
			
			// ComputerCraft only needs to update 20 times a second.
			Thread.sleep(1000 / 20)
		}
	}
	
	override run() {
		window = new EmulatorWindow().using [
			visible = true
			toFront
			requestFocus
		]
		
		startLoop()
	}
}
		