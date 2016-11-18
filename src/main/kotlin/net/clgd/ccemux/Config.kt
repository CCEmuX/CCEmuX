package net.clgd.ccemux

import net.clgd.ccemux.Utils.parseInt
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.util.*

class Config(val dataDir: Path) : Properties() {
	val CONFIG_FILE_NAME = "ccemux.properties"

	val configFile = dataDir.resolve(CONFIG_FILE_NAME).toFile()

	private val listeners = ArrayList<Runnable>()

	init {
		load(Config::class.java.getResourceAsStream("/default.properties"))
		load(Config::class.java.getResourceAsStream("/cc.properties"))

		if (!configFile.exists()) {
			FileOutputStream(configFile).use {
				f -> IOUtils.copy(Config::class.java.getResourceAsStream("/default.properties"), f)
			}
		}

		load(FileInputStream(configFile))
	}

	fun addListener(listener: Runnable) {
		synchronized(listeners, {
			listeners.add(listener)
			listener.run()
		})
	}

	override fun setProperty(key: String, value: String): Any {
		val previous = super.setProperty(key, value)
		synchronized(listeners, {
			listeners.forEach(Runnable::run)
		})
		return previous
	}

	override fun load(inStream: InputStream) {
		super.load(inStream)
		synchronized(listeners, {
			listeners.forEach(Runnable::run)
		})
	}

	fun getTermWidth() = getProperty("termWidth").parseInt()

	fun setTermWidth(width: Int) = setProperty("termWidth", width.toString())

	fun getTermHeight() = getProperty("termHeight").parseInt()

	fun setTermHeight(height: Int) = setProperty("termHeight", height.toString())

	fun getTermScale() = getProperty("termScale").parseInt()

	fun isApiEnabled() = getProperty("apiEnabled") == "true"

	fun getRenderer() = getProperty("renderer").split(',').map(String::trim)

	fun getCCModule() = getProperty("ccModule")

	fun getCCRevision() = getProperty("ccRevision")

	fun getCCExt() = getProperty("ccExt")

	fun getCCChecksum() = getProperty("ccChecksum")

	fun getCCPatternRemote() = getProperty("ccPatternRemote")

	fun getCCPatternLocal() = getProperty("ccPatternLocal")

	fun getCCRemote() = getCCPatternRemote()
		.replace("[module]", getCCModule())
		.replace("[revision]", getCCRevision())
		.replace("[ext]", getCCExt())

	fun getCCLocal() = getCCPatternLocal()
		.replace("[module]", getCCModule())
		.replace("[revision]", getCCRevision())
		.replace("[ext]", getCCExt())

	fun getCCTweaks() = getProperty("cctweaks.enabled") == "true"

	fun saveProperties() {
		FileOutputStream(configFile).use {
			f -> store(f, null)
		}
	}
}
