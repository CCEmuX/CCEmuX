package net.clgd.ccemux.terminal

import dan200.computercraft.ComputerCraft
import dan200.computercraft.core.terminal.Terminal
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JComponent
import net.clgd.ccemux.CCEmuX
import net.clgd.ccemux.Utils
import org.eclipse.xtend.lib.annotations.Accessors

class TerminalComponent extends JComponent {
	static val CC_FONT_PATH = "/assets/computercraft/textures/gui/termFont.png"
	
	@Accessors(PUBLIC_GETTER) Terminal terminal
	@Accessors(PUBLIC_GETTER) int pixelWidth
	@Accessors(PUBLIC_GETTER) int pixelHeight
	
	@Accessors char cursorChar = '_'
	
	@Accessors boolean blinkLocked = false
	
	BufferedImage[] fontImages
	
	new(Terminal terminal, int pixelWidth, int pixelHeight) {	
		this.pixelWidth = pixelWidth
		this.pixelHeight = pixelHeight
		
		this.terminal = terminal
		
		fontImages = newArrayOfSize(16)
		
		val baseImage = ImageIO.read((ComputerCraft).getResource(CC_FONT_PATH))
		
		for (var i = 0; i < fontImages.length; i++) {
			fontImages.set(i, Utils.makeTintedCopy(baseImage, Utils.getCCColourFromInt(i)))
		}
	
		val termDimensions = new Dimension(terminal.width * pixelWidth, terminal.height * pixelHeight) 
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
	
	private def drawChar(Graphics it, char c, int x, int y, int colour) {
		if (c as int == 0) {
			// Nothing to do here.
			return
		}
		
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
			c,
			charWidth, charHeight,
			fontWidth, fontHeight
		)
		
		drawImage(
			fontImages.get(colour),
			
			// Destination
			x, y,
			x + pixelWidth, y + pixelHeight,
			
			// Source
			charLocation.x, charLocation.y,
			charLocation.x + charWidth, charLocation.y + charHeight,
			
			null
		)
	}
	
	override paintComponent(Graphics it) {
		for (var y = 0; y < terminal.height; y++) {
			val textLine = terminal.getLine(y)
			val bgLine = terminal.getBackgroundColourLine(y)
			val fgLine = terminal.getTextColourLine(y)
			
			for (var x = 0; x < terminal.width; x++) {
				color = Utils.getCCColourFromChar(bgLine.charAt(x))
				fillRect(x * pixelWidth, y * pixelHeight, pixelWidth, pixelHeight)
				
				val character = textLine.charAt(x)
				drawChar(it, character, x * pixelWidth, y * pixelHeight, Utils.base16ToInt(fgLine.charAt(x)))
			}
		}
		
		val blink =
			if (!terminal.cursorBlink) {
				false
			} else {
				if (blinkLocked) {
				 	true
				} else {
			 		CCEmuX.get.globalCursorBlink
			 	} 	
		 	}
		
		if (blink) {
			drawChar(
				it, cursorChar,
				terminal.cursorX * pixelWidth, terminal.cursorY * pixelHeight,
				terminal.textColour
			)
		}
	}
}