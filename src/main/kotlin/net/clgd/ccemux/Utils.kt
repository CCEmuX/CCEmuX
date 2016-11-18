package net.clgd.ccemux

import dan200.computercraft.shared.util.Colour
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.Transparency
import java.awt.image.BufferedImage

object Utils {
	val BASE_16 = "0123456789abcdef"

	/**
	 * Extension method to make option generation a bit prettier
	 */
	fun Options.buildOpt(shortName: String, f: (Option.Builder) -> Unit) {
		val b = Option.builder(shortName)
		f(b)
		addOption(b.build())
	}

	fun <T> Iterable<T>.any(f: (T) -> Boolean): Boolean {
		for (t in this) {
			if (f(t)) {
				return true
			}
		}

		return false
	}

	fun <T> Iterable<T>.first(f: (T) -> Boolean): T? {
		for (t in this) {
			if (f(t)) {
				return t
			}
		}

		return null
	}

	/**
	 * Takes a colour id (see http://www.computercraft.info/wiki/Colors_(API)#Colors "Paint" column),
	 * and returns a {@link Color}.
	 */
	fun getCCColourFromInt(i: Int): Color {
		val col = Colour.fromInt(15 - i)
		return if (col == null) Color.WHITE else Color(col.r, col.g, col.b)
	}

	fun getCCColourFromChar(c: Char) = getCCColourFromInt(base16ToInt(c))

	fun BufferedImage.makeTintedCopy(tint: Color): BufferedImage {
		val gc = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration

		var tintedImg = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT)

		for (y in 0..(height - 1)) {
			for (x in 0..(width - 1)) {
				val rgb = getRGB(x, y)

				if (rgb != 0) {
					tintedImg.setRGB(x, y, tint.getRGB())
				}
			}
		}

		return tintedImg
	}

	fun base16ToInt(c: Char) = BASE_16.indexOf(c.toString().toLowerCase())

	fun String.trimSlashes() = this.replace(Regex("/$"), "")

	fun String.parseInt() = Integer.parseInt(this)
}
