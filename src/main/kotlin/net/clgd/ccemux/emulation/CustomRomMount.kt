package net.clgd.ccemux.emulation

import dan200.computercraft.api.filesystem.IMount
import dan200.computercraft.core.filesystem.FileSystem
import net.clgd.ccemux.Utils.trimSlashes
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import java.util.zip.ZipInputStream

// here be dragons
class CustomRomMount(i: ZipInputStream) : IMount {
	val folders = HashMap<String, MutableMap<String, ByteArray>>()

	init {
		folders.put("", hashMapOf())

		while (true) {
			val entry = i.nextEntry ?: break

			if (entry.isDirectory) {
				folders.put(entry.name.trimSlashes(), hashMapOf())
			} else {
				val n = entry.name.trimSlashes()
				val data = ByteArray(entry.size.toInt())
				IOUtils.read(i, data)
				folders[FileSystem.getDirectory(n)]?.put(FileSystem.getName(n), data)
			}
		}
	}

	override fun exists(path: String): Boolean {
		return folders.containsKey(path) ||
			   folders[FileSystem.getDirectory(path)]?.containsKey(FileSystem.getName(path)) ?: false
	}

	override fun getSize(path: String?): Long {
		return folders.get(FileSystem.getDirectory(path))?.get(FileSystem.getName(path))?.size?.toLong() ?: 0L
	}

	override fun isDirectory(path: String) = folders.containsKey(path)

	override fun list(path: String, contents: MutableList<String>?) {
		folders.get(path)?.keys?.forEach { f -> if (!(contents?.contains(f) ?: true)) contents?.add(f) }

		folders.keys.forEach {
			it -> run {
				var f = it

				do {
					if (FileSystem.getDirectory(f) == path) {
						val n = FileSystem.getName(f)
						if (!(contents?.contains(n) ?: true)) contents?.add(n)
					}

					f = FileSystem.getDirectory(f)
				} while (f != "" && f != "..")
			}
		}
	}

	override fun openForRead(path: String): InputStream {
		return ByteArrayInputStream(folders.get(FileSystem.getDirectory(path))?.get(FileSystem.getName(path)))
	}
}
