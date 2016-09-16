package net.clgd.ccemux.terminal

import java.util.Random

class TerminalLayer {
	TerminalPixel[] pixels
	
	int width
	int height
	
	new(int width, int height) {
		this.width = width
		this.height = height
		
		pixels = newArrayOfSize(width * height)
		clear(0xF)
	}
	
	def clear(int backgroundColour) {
		for (var y = 0; y < height; y++) {
			for (var x = 0; x < width; x++) {
				setPixel(x, y, new TerminalPixel(backgroundColour, 0x0, ' '))
			}
		}
	}
	
	def randomise() {
		val random = new Random()
		
		for (var y = 0; y < height; y++) {
			for (var x = 0; x < width; x++) {
				setPixel(x, y, new TerminalPixel(random.nextInt(0xF), random.nextInt(0xF), ' '))
			}
		}
	}
	
	def getPixel(int x, int y) {
		return pixels.get(y * width + x)
	}
	
	def setPixel(int x, int y, TerminalPixel pixel) {
		pixels.set(y * width + x, pixel)
	}
	
	def getWidth() {
		return width
	}
	
	def getHeight() {
		return height
	}
}