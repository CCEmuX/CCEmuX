package net.clgd.ccemux.emulation

import dan200.computercraft.api.filesystem.IMount
import java.io.IOException
import java.util.List

import static extension net.clgd.ccemux.Utils.*

class CustomRomProgram implements IMount {
	val String resource
	val String name

	new(String resource, String name) {
		this.resource = resource
		this.name = name
	}

	private def stream() {
		CustomRomProgram.getResourceAsStream(resource)
	}

	override exists(String path) throws IOException {
		return path == "programs/" + name || path == "programs"
	}

	override getSize(String path) throws IOException {
		return if (exists(path)) stream.with[available] else 0
	}

	override isDirectory(String path) throws IOException {
		return path == "programs"
	}

	override list(String path, List<String> contents) throws IOException {
		if (path == "programs") contents.add(name)
	}

	override openForRead(String path) throws IOException {
		if (path == "programs/" + name) stream
	}

}