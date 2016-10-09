package net.clgd.ccemux.rendering.swing

import java.util.Random
import org.eclipse.xtend.lib.annotations.Accessors

package class TerminalLayer {
	TerminalPixel[] pixels
	
	@Accessors(PUBLIC_GETTER) int width
	@Accessors(PUBLIC_GETTER) int height
	
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
		val random = new Random
		
		for (var y = 0; y < height; y++) {
			for (var x = 0; x < width; x++) {
				setPixel(x, y, new TerminalPixel(
					random.nextInt(0xF), random.nextInt(0xF), random.nextInt(0xFF) as char
				))
			}
		}
	}
	
	@Pure
	def getPixel(int x, int y) {
		return pixels.get(y * width + x)
	}
	
	def void setPixel(int x, int y, TerminalPixel pixel) {
		pixels.set(y * width + x, pixel)
	}
}