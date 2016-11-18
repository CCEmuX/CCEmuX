package net.clgd.ccemux.rendering.tror

import java.io.InputStream
import java.util.ArrayList
import java.util.Scanner

class AsyncScanner implements Runnable {
	val Scanner input
	val lines = new ArrayList<String>

	public new(InputStream input) {
		this.input = new Scanner(input)
	}

	def hasLines() {
		lines.size > 0
	}

	def getLines() {
		val out = lines.clone as ArrayList<String>
		lines.clear
		return out
	}

	def start() {
		return new Thread(this) => [
			daemon = true
			start
		]
	}

	override run() {
		while (input.hasNextLine) {
			lines.add(input.nextLine)
		}
	}
}
