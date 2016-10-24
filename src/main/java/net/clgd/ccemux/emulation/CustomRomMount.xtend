package net.clgd.ccemux.emulation

import dan200.computercraft.api.filesystem.IMount
import dan200.computercraft.core.filesystem.FileSystem
import java.io.ByteArrayInputStream
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import org.apache.commons.io.IOUtils

// here be dragons
class CustomRomMount implements IMount {
	val Map<String, Map<String, byte[]>> folders
	
	@Pure
	def static trimSlash(String path) {
		return path.replaceAll("/$", "")
	} 
	
	new(ZipInputStream is) {
		//folders = #{"" -> newHashMap(), "test" -> #{"test2" -> "test contents".bytes}}
		folders = new HashMap
		
		folders.put("", newHashMap)
		
		var ZipEntry entry
		while ((entry = is.nextEntry) != null) {
			if (entry.directory) {
				folders.put(println(trimSlash(entry.name)), newHashMap())
			} else {
				val n = println(trimSlash(entry.name))
				var data = newByteArrayOfSize(entry.size as int)
				IOUtils.read(is, data)
				folders.get(FileSystem.getDirectory(n)).put(FileSystem.getName(n), data)
			}
		}
	}
	
	override exists(String path) {
		folders.containsKey(path) || folders.get(FileSystem.getDirectory(path))?.containsKey(FileSystem.getName(path))
	}
	
	override getSize(String path) {
		folders.get(FileSystem.getDirectory(path))?.get(FileSystem.getName(path))?.size
	}
	
	override isDirectory(String path) {
		folders.containsKey(path)
	}
	
	override list(String path, List<String> contents) {
		folders.get(path)?.keySet.forEach[if (!contents.contains(it)) contents.add(it)]
		
		folders.keySet.forEach[
			var f = it
			
			do {
				if (FileSystem.getDirectory(f) == path) {
					FileSystem.getName(f) => [if (!contents.contains(it)) contents.add(it)]
				}
				f = FileSystem.getDirectory(f)
			} while (f != "" && f != "..")
		]
	}
	
	override openForRead(String path) {
		new ByteArrayInputStream(folders.get(FileSystem.getDirectory(path))?.get(FileSystem.getName(path)))
	}
}