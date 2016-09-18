package net.clgd.ccemux.emulation

import dan200.computercraft.api.filesystem.IMount
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Paths
import java.util.List

class ClasspathMount implements IMount {
	var basePath = "/"
	Class<?> clazz
	
	new(Class<?> clazz, String basePath) {
		this.basePath = basePath
		this.clazz = clazz
	}
	
	private def formatPath(String path) {
		var formatted = Paths.get(basePath, path ?: "").toString
		formatted = formatted.replace('\\', '/')
		
		if (!formatted.startsWith("/")) {
			formatted = "/" + formatted
		}
		
		return formatted
	} 
	
	override exists(String path) throws IOException {
		return clazz.getResource(formatPath(path)) != null
	}
	
	override getSize(String path) throws IOException {
		val is = clazz.getResourceAsStream(formatPath(path))
		val out = new ByteArrayOutputStream()
		val buffer = newByteArrayOfSize(1024)
		
		var count = 0
		
		while ((count = is.read(buffer)) > 0) {
			out.write(buffer, 0, count)
		}
		
		val len = out.size
		
		out.close
		is.close
		
		return len
	}
	
	override isDirectory(String path) throws IOException {
		return new File(clazz.getResource(formatPath(path)).path).directory
	}
	
	override list(String path, List<String> contents) throws IOException {
		val file = new File(clazz.getResource(formatPath(path)).path)
		contents.addAll(file.list)
	}
	
	override InputStream openForRead(String path) throws IOException {
		return clazz.getResourceAsStream(formatPath(path))
	}
}