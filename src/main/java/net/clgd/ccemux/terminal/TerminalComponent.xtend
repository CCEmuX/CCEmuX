package net.clgd.ccemux.terminal

import dan200.computercraft.ComputerCraft
import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JComponent
import net.clgd.ccemux.Utils
import org.eclipse.xtend.lib.annotations.Accessors

class TerminalComponent extends JComponent {
	static val CC_FONT_PATH = "/assets/computercraft/textures/gui/termFont.png"
	
	@Accessors(PUBLIC_GETTER) TerminalLayer terminal
	@Accessors(PUBLIC_GETTER) int pixelWidth
	@Accessors(PUBLIC_GETTER) int pixelHeight
	
	BufferedImage fontImage
	
	new(int width, int height, int pixelWidth, int pixelHeight) {	
		this.pixelWidth = pixelWidth
		this.pixelHeight = pixelHeight
		
		terminal = new TerminalLayer(width, height)
		terminal.randomise
	
		fontImage = try {
			ImageIO.read(typeof(ComputerCraft).getResource(CC_FONT_PATH))
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load termFont.png")
		}
		
		val termDimensions = new Dimension(width * pixelWidth, height * pixelHeight) 
		size = termDimensions
		preferredSize = termDimensions
	}
	
	protected override paintComponent(Graphics it) {
		for (var y = 0; y < terminal.height; y++) {
			for (var x = 0; x < terminal.width; x++) {
				val pixel = terminal.getPixel(x, y)
				
				color = Utils.getCCColourFromInt(pixel.backgroundColour)
				fillRect(x * pixelWidth, y * pixelHeight, pixelWidth, pixelHeight)
			}
		}
	}
}