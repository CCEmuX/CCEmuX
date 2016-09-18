package net.clgd.ccemux

import dan200.computercraft.ComputerCraft
import java.awt.SplashScreen
import java.nio.file.Paths
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.eclipse.xtend.lib.annotations.Accessors
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static extension net.clgd.ccemux.Utils.*

class CCEmuX {
	@Accessors(PUBLIC_GETTER) static Logger logger
	@Accessors(PUBLIC_GETTER) static EmulatorWindow window
	@Accessors(PUBLIC_GETTER) static Config conf
	@Accessors(PUBLIC_GETTER) static var portable = false
	
	def static getDataDir() {
		if (portable)
			Paths.get("")
		else
			// TODO: Use appdata/local share/whatever when not in portable mode
			Paths.get("")
	}
	
	def static void main(String[] args) {
		val opts = new Options().using [
			buildOpt("h") [
				longOpt("help")
				desc("Shows the help information")
			]

			buildOpt("p") [
				longOpt("portable")

				desc("Forces portable mode, in which all files (configs, saves, libraries) are kept in the same folder as CCEmuX." +
					"Will automatically be enabled if the config file is in the same folder as CCEmuX.")
			]
			
			buildOpt(null) [
				longOpt("debug")
				
				desc("Enables more verbose debug-level logging")
			]
		]

		val cmd = new DefaultParser().parse(opts, args)

		portable = cmd.hasOption('p')

		if (cmd.hasOption("debug")) System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")

		logger = LoggerFactory.getLogger("CCEmuX")
		
		logger.info("Starting CCEmuX...")
		
		logger.debug("Data directory is {}", dataDir.toAbsolutePath.toString)
		
		logger.debug("Loading configuration data...", dataDir.resolve(Config.CONFIG_FILE_NAME).toString)
		conf = new Config(dataDir.resolve(Config.CONFIG_FILE_NAME).toFile)
		logger.debug("Configuration data loaded.")

		CCBootstrapper.loadCC
		logger.info("Loaded CC (v{})", ComputerCraft.version)
		
		if (ComputerCraft.version != conf.CCRevision) logger.warn("Potential compatibility issues detected - expected CC version (v{}) does not match loaded CC version (v{})", conf.CCRevision, ComputerCraft.version)
		
		window = new EmulatorWindow().using [
			// close splash screen before making frame visible
			// prevents occasional window focus changes
			SplashScreen.splashScreen?.close
			
			visible = true
		]
	}
}
		