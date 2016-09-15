package net.clgd.ccemux

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.Properties
import org.apache.commons.io.FileUtils

class CCBootstrapper {
	/**
	 * Downloads the CC jar if necessary, and then returns the {@code File}.
	 */
	def static File getCCJar(File jar) {
		if (!jar.exists) {
			val props = new Properties()
			props.load(CCBootstrapper.getResourceAsStream("/gradle.properties"))

			val downloadLink = props.getProperty("ccPattern").replace("[module]", "ComputerCraft").replace("[revision]",
				props.getProperty("ccVersion")).replace("[ext]", "jar")

			FileUtils.copyURLToFile(new URL(downloadLink), jar)
		}
		
		return jar
	}
	
	/**
	 * Downloads the CC jar if necessary, then return the {@code File}. The jar will be downloaded to ./ComputerCraft.jar
	 */
	def static File getCCJar() {
		return getCCJar(new File("ComputerCraft.jar"))
	}

	def static boolean isCCPresent() {
		try {
			Class.forName("dan200.computercraft.ComputerCraft", false, ClassLoader.systemClassLoader)
			return true
		} catch (Exception e) {
			return false
		}
	}

	def static void loadCC() {
		if (!CCPresent) {
			val classloader = ClassLoader.systemClassLoader as URLClassLoader
			val method = URLClassLoader.getDeclaredMethod("addURL", (URL))
			method.accessible = true
			method.invoke(classloader, CCJar.toURI.toURL)
		}
	}
}
