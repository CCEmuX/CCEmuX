package net.clgd.ccemux.rendering.tror

import java.io.Closeable
import java.io.InputStream
import java.util.*

class AsyncScanner(inputStream: InputStream) : Runnable, Closeable {
	val input = Scanner(inputStream)
	val lines = ArrayList<String>()
	val thread = Thread(this)

	private var keepRunning = false

	init {
		thread.isDaemon = true
	}

	fun hasLines() = lines.isNotEmpty()

	fun getLinesList(): ArrayList<String> {
		@Suppress("UNCHECKED_CAST")
		val out = lines.clone() as ArrayList<String>
		lines.clear()
		return out
	}

	fun start() = thread.start()

	override fun run() {
		keepRunning = true

		while (keepRunning && input.hasNextLine()) {
			lines.add(input.nextLine())
		}

		input.close()
	}

	override fun close() {
		keepRunning = false
	}
}
