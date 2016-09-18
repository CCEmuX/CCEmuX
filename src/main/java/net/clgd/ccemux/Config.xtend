package net.clgd.ccemux

import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.Properties

import static extension net.clgd.ccemux.Utils.*

class Config extends Properties {
	@Pure
	def private static int parseInt(String str) {
		Integer.parseInt(str)
	}
	
	public static val CONFIG_FILE_NAME = "ccemux.properties"
	
	new(File configFile) {
		// load default properties from embedded resources
		super(new Properties().using [
			load(Config.getResourceAsStream("/default.properties"))
			load(Config.getResourceAsStream("/cc.properties"))
		])
		
		if (configFile.exists)
			load(new FileInputStream(configFile))
	}
	
	@Pure
	def getTermWidth() {
		getProperty("termWidth").parseInt
	}
	
	@Pure
	def getTermHeight() {
		getProperty("termHeight").parseInt
	}
	
	@Pure
	def getTermScale() {
		getProperty("termScale").parseInt
	}
	
	@Pure
	def getCCModule() {
		getProperty("ccModule")
	}
	
	@Pure
	def getCCRevision() {
		getProperty("ccRevision")
	}
	
	@Pure
	def getCCExt() {
		getProperty("ccExt")
	}
	
	@Pure
	def getCCPatternRemote() {
		getProperty("ccPatternRemote")
	}
	
	@Pure
	def getCCPatternLocal() {
		getProperty("ccPatternLocal")
	}
	
	@Pure
	def getCCRemote() {
		new URL(CCPatternRemote.replace("[module]", CCModule).replace("[revision]", CCRevision).replace("[ext]", CCExt))
	}
	
	@Pure
	def getCCLocal() {
		CCPatternLocal.replace("[module]", CCModule).replace("[revision]", CCRevision).replace("[ext]", CCExt)
	}
}

class ConfigException extends Exception {
	
}