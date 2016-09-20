package net.clgd.ccemux

import java.net.URL
import java.net.URLClassLoader
import org.apache.commons.io.FileUtils
import org.slf4j.Logger

class CCBootstrapper {

	def static boolean isCCPresent() {
		try {
			Class.forName("dan200.computercraft.ComputerCraft", false, ClassLoader.systemClassLoader)
			return true
		} catch (Exception e) {
			return false
		}
	}

	def static getCCJar() {
		CCEmuX.get.dataDir.resolve(CCEmuX.get.conf.CCLocal).toFile
	}

	def static void loadCC(Logger logger) {
		if (!CCPresent) {
			logger.debug("CC not on classpath, bootstrapping...")
			
			if (!CCJar.exists) {
				logger.info("CC jar not found in expected location, attempting download to {}", CCJar.absolutePath)
				FileUtils.copyURLToFile(CCEmuX.get.conf.CCRemote, CCJar, 10000, 10000)
			}
			
			val classloader = ClassLoader.systemClassLoader as URLClassLoader
			val method = URLClassLoader.getDeclaredMethod("addURL", (URL))
			method.accessible = true
			method.invoke(classloader, CCJar.toURI.toURL)
		}
	}
}
