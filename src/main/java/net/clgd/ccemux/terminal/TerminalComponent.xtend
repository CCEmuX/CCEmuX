package net.clgd.ccemux.terminal

import dan200.computercraft.shared.util.Colour
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent
import org.eclipse.xtend.lib.annotations.Accessors

class TerminalComponent extends JComponent {
	@Accessors(PUBLIC_GETTER) TerminalLayer terminal
	@Accessors(PUBLIC_GETTER) int pixelWidth
	@Accessors(PUBLIC_GETTER) int pixelHeight
	
	new(int width, int height, int pixelWidth, int pixelHeight) {	
		this.pixelWidth = pixelWidth
		this.pixelHeight = pixelHeight
		
		terminal = new TerminalLayer(width, height)
		terminal.randomise
		
		val termDimensions = new Dimension(width * pixelWidth, height * pixelHeight) 
		size = termDimensions
		preferredSize = termDimensions
	}
	
	@Pure
	private def Color getColourFromInt(int i) {
		val col = Colour.fromInt(15 - i)
		
		if (col == null) {
			return Color.WHITE
		}
		
		return new Color(col.r, col.g, col.b)
	}
	
	protected override paintComponent(Graphics it) {
		for (var y = 0; y < terminal.height; y++) {
			for (var x = 0; x < terminal.width; x++) {
				val pixel = terminal.getPixel(x, y)
				
				color = getColourFromInt(pixel.backgroundColour)
				fillRect(x * pixelWidth, y * pixelHeight, pixelWidth, pixelHeight)
			}
		}
	}
}