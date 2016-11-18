package net.clgd.ccemux.rendering.awt

import dan200.computercraft.ComputerCraft
import dan200.computercraft.core.terminal.Terminal
import net.clgd.ccemux.Utils
import net.clgd.ccemux.Utils.makeTintedCopy
import net.clgd.ccemux.emulation.CCEmuX
import java.awt.Canvas
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class TerminalComponent(val terminal: Terminal, termScale: Int) : Canvas() {
	val CC_FONT_PATH = "/assets/computercraft/textures/gui/termFont.png"

	val pixelWidth = 6 * termScale
	val pixelHeight = 9 * termScale
	val margin = 2 * termScale

	var cursorChar = '_'

	var blinkLocked = false

	var fontImages: Array<BufferedImage> = arrayOf()

	init {
		val baseImage = ImageIO.read(ComputerCraft::class.java.getResource(CC_FONT_PATH))
		fontImages = Array(16, { i -> baseImage.makeTintedCopy(Utils.getCCColourFromInt(i)) })
		resizeTerminal(terminal.width, terminal.height)
	}

	fun getCharLocation(c: Char, charWidth: Int, charHeight: Int, fontWidth: Int, fontHeight: Int): Point {
		val columns = fontWidth / charWidth
		val rows = fontHeight / charHeight

		val charCode = c.toInt()

		return Point(
			(charCode % columns) * charWidth,
			(charCode / rows) * charHeight
		)
	}

	fun drawChar(g: Graphics, c: Char, x: Int, y: Int, colour: Int) {
		if (c.toInt() == 0) {
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

		val charLocation = getCharLocation(c, charWidth, charHeight, fontWidth, fontHeight)

		g.drawImage(
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

	fun renderTerminal(dt: Float) {
		synchronized(terminal) {
			val g = bufferStrategy.drawGraphics

			var dx = 0
			var dy = 0

			for (y in 0..(terminal.height-1)) {
				val textLine = terminal.getLine(y)
				val bgLine = terminal.getBackgroundColourLine(y)
				val fgLine = terminal.getTextColourLine(y)

				var height = if (y == 0 || y == terminal.height - 1) pixelHeight + margin else pixelHeight

				for (x in 0..(terminal.width-1)) {
					var width = if (x == 0 || x == terminal.width - 1) pixelWidth + margin else pixelWidth

					g.color = Utils.getCCColourFromChar(bgLine?.charAt(x) ?: 'f')
					g.fillRect(dx, dy, width, height)

					val character: Char = textLine?.charAt(x) ?: ' '
					val fgChar: Char = fgLine?.charAt(x) ?: 'f'
					drawChar(g, character, x * pixelWidth + margin, y * pixelHeight + margin, Utils.base16ToInt(fgChar))

					dx += width
				}

				dx = 0
				dy += height
			}

			val blink = terminal.cursorBlink && (blinkLocked || CCEmuX.getGlobalCursorBlink())

			if (blink) {
				drawChar(
					g, cursorChar,
					terminal.cursorX * pixelWidth + margin, terminal.cursorY * pixelHeight + margin,
					terminal.textColour
				)
			}

			g.dispose()
		}
	}

	fun render(dt: Float) {
		if (bufferStrategy == null) {
			createBufferStrategy(2)
		}

		do {
			do {
				renderTerminal(dt)
			} while (bufferStrategy.contentsRestored())

			bufferStrategy.show()
		} while (bufferStrategy.contentsLost())
	}

	fun resizeTerminal(width: Int, height: Int) {
		val termDimensions = Dimension(width * pixelWidth + margin * 2, height * pixelHeight + margin * 2)
		size = termDimensions
		preferredSize = termDimensions
	}
}
