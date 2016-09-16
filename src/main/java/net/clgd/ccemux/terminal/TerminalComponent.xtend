package net.clgd.ccemux.terminal

import dan200.computercraft.shared.util.Colour
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

class TerminalComponent extends JComponent {
	TerminalLayer terminal
	
	int pixelWidth
	int pixelHeight
	
	new(int width, int height, int pixelWidth, int pixelHeight) {	
		this.pixelWidth = pixelWidth
		this.pixelHeight = pixelHeight
		
		terminal = new TerminalLayer(width, height)
		terminal.randomise
		
		val termDimensions = new Dimension(width * pixelWidth, height * pixelHeight) 
		size = termDimensions
		preferredSize = termDimensions
	}
	
	def getPixelWidth() {
		return pixelWidth
	}
	
	def getPixelHeight() {
		return pixelHeight
	}
	
	def getLayer() {
		return terminal
	}
	
	private def Color getColourFromInt(int i) {
		val col = Colour.fromInt(15 - i)
		
		if (col == null) {
			return Color.WHITE
		}
		
		return new Color(col.r, col.g, col.b)
	}
	
	protected override paintComponent(Graphics g) {
		for (var y = 0; y < terminal.height; y++) {
			for (var x = 0; x < terminal.width; x++) {
				val pixel = terminal.getPixel(x, y)
				g.color = getColourFromInt(pixel.backgroundColour)
				g.fillRect(x * pixelWidth, y * pixelHeight, pixelWidth, pixelHeight)
			}
		}
	}
}