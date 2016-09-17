package net.clgd.ccemux.terminal

import dan200.computercraft.ComputerCraft
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
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
	
	BufferedImage[] fontImages
	
	new(int width, int height, int pixelWidth, int pixelHeight) {	
		this.pixelWidth = pixelWidth
		this.pixelHeight = pixelHeight
		
		fontImages = newArrayOfSize(16)
		
		val BufferedImage baseImage = ImageIO.read(typeof(ComputerCraft).getResource(CC_FONT_PATH))
		
		for (var i = 0; i < fontImages.length; i++) {
			fontImages.set(i, Utils.makeTintedCopy(baseImage, Utils.getCCColourFromInt(i)))
		}
		
		terminal = new TerminalLayer(width, height)
		terminal.randomise
	
		val termDimensions = new Dimension(width * pixelWidth, height * pixelHeight) 
		size = termDimensions
		preferredSize = termDimensions
	}

	@Pure
	private static def getCharLocation(char c, int charWidth, int charHeight, int fontWidth, int fontHeight) {
		val columns = fontWidth / charWidth
		val rows = fontHeight / charHeight
		
		val charCode = c as int
		
		return new Point(
			(charCode % columns) * charWidth,
			(charCode / rows) * charHeight
		)
	}
	
	protected override paintComponent(Graphics it) {
		for (var y = 0; y < terminal.height; y++) {
			for (var x = 0; x < terminal.width; x++) {
				val pixel = terminal.getPixel(x, y)
				
				color = Utils.getCCColourFromInt(pixel.backgroundColour)
				fillRect(x * pixelWidth, y * pixelHeight, pixelWidth, pixelHeight)
				
				// Retrieve the location of the character in the font bitmap and
				// render the appropriate subrect.
				
				if ((pixel.character as int) != 0) {
					// TODO: These are width & height of a character in the font bitmap.
					// Replace these with something non-magical.
					val charWidth = 6
					val charHeight = 9
					
					// TODO: Newer CC versions pad the font texture with empty space to make it POT.
					// Therefore, we need the actual space occupied by the texture.
					// Replace these with something non-magical.
					val fontWidth = 96
					val fontHeight = 144
					
					val charLocation = getCharLocation(
						pixel.character,
						charWidth, charHeight,
						fontWidth, fontHeight
					)
					
					drawImage(
						fontImages.get(pixel.foregroundColour),
						
						// Destination
						x * pixelWidth, y * pixelHeight,
						x * pixelWidth + pixelWidth, y * pixelHeight + pixelHeight,
						
						// Source
						charLocation.x, charLocation.y,
						charLocation.x + charWidth, charLocation.y + charHeight,
						
						null
					)
				}
			}
		}
	}
}