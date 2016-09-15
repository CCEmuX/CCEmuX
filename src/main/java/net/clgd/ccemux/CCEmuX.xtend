package net.clgd.ccemux

import dan200.computercraft.ComputerCraft
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class CCEmuX {
	public final Logger logger
	
	public final String ccVersion
	
	new() {
		logger = LoggerFactory.getLogger((CCEmuX))
		
		logger.info("Starting CCEmuX")
		
		logger.info("Loading ComputerCraft")
		CCBootstrapper.loadCC
		
		ccVersion = ComputerCraft.version
		logger.info("CC version is {}", ccVersion)
	}
}