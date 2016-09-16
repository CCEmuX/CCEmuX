package net.clgd.ccemux

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import dan200.computercraft.ComputerCraft

class CCEmuX {
	static Logger logger
	
	def static void main(String[] args) {
		logger = LoggerFactory.getLogger("CCEmuX")
		logger.info("Starting CCEmuX")
		
		CCBootstrapper.loadCC
		logger.info("Using CC Version {}", ComputerCraft.version)
	}
}