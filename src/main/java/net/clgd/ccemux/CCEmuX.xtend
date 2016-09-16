package net.clgd.ccemux

import dan200.computercraft.ComputerCraft
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.eclipse.xtend.lib.annotations.Accessors
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static net.clgd.ccemux.Utils.using

import static extension net.clgd.ccemux.Utils.buildOpt

class CCEmuX {
	@Accessors(PUBLIC_GETTER) static Logger logger
	@Accessors(PUBLIC_GETTER) static EmulatorWindow window
	@Accessors(PUBLIC_GETTER) static var portable = false


	def static void main(String[] args) {
		val opts = using(new Options) [
			buildOpt("h") [
				longOpt("help")
				desc("Shows the help information")
			]

			buildOpt("p") [
				longOpt("portable")

				desc("Forces portable mode, in which all files (configs, saves, etc) are kept in the same folder as CCEmuX." +
					"Will automatically be enabled if the config file is in the same folder as CCEmuX.")
			]
		]

		val cmd = new DefaultParser().parse(opts, args)

		if (cmd.hasOption('p')) portable = true

		logger = LoggerFactory.getLogger("CCEmuX")
		
		logger.info("Starting CCEmuX")

		CCBootstrapper.loadCC
		logger.info("Using CC Version {}", ComputerCraft.version)

		window = using(new EmulatorWindow) [
			visible = true
		]
	}
}
		