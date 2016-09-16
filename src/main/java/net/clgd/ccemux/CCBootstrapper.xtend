package net.clgd.ccemux

import java.net.URL
import java.net.URLClassLoader
import java.util.Properties

class CCBootstrapper {

	def static URL ccURL() {
		val propres = CCBootstrapper.getResourceAsStream("/gradle.properties")

		if (propres == null)
			throw new Exception("CC classes not present and no properties available for automatic loading")

		val props = new Properties()

		props.load(propres)
		return new URL(
			"jar:" +
				props.getProperty("ccPattern").replace("[module]", "ComputerCraft").replace("[revision]",
					props.getProperty("ccVersion")).replace("[ext]", "jar") + "!/")
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
			method.invoke(classloader, ccURL)
		}
	}
}
