package net.clgd.ccemux

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import dan200.computercraft.ComputerCraft
import org.eclipse.swt.widgets.Display

class CCEmuX {
	static Logger logger
	static EmulatorWindow window
	
	def static void main(String[] args) {
		logger = LoggerFactory.getLogger("CCEmuX")
		logger.info("Starting CCEmuX")
		
		CCBootstrapper.loadCC
		logger.info("Using CC Version {}", ComputerCraft.version)
		
		val display = new Display()
		window = new EmulatorWindow(display)
		
		display.dispose
	}
}