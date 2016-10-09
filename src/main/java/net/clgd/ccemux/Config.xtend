package net.clgd.ccemux

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

import static extension org.apache.commons.io.IOUtils.copy

class Config extends Properties {
	@Pure
	def private static int parseInt(String str) {
		Integer.parseInt(str)
	}
	
	public static val CONFIG_FILE_NAME = "ccemux.properties"
	
	new(File configFile) {
		// load default properties from embedded resources
		super(new Properties => [
			load(Config.getResourceAsStream("/default.properties"))
			load(Config.getResourceAsStream("/cc.properties"))
		])
		
		if (configFile.exists) {
			load(new FileInputStream(configFile))
		} else {
			new FileOutputStream(configFile) => [
				Config.getResourceAsStream("/default.properties").copy(it)
				//Config.getResourceAsStream("/cc.properties").copy(it)
				
				flush
				close
			]
		}
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
	def getRenderer() {
		getProperty("renderer")
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
	def getCCChecksum() {
		getProperty("ccChecksum")
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
		CCPatternRemote.replace("[module]", CCModule).replace("[revision]", CCRevision).replace("[ext]", CCExt)
	}
	
	@Pure
	def getCCLocal() {
		CCPatternLocal.replace("[module]", CCModule).replace("[revision]", CCRevision).replace("[ext]", CCExt)
	}
}