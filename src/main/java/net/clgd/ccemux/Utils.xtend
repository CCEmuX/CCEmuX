package net.clgd.ccemux

import dan200.computercraft.shared.util.Colour
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.util.function.Consumer
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.util.function.Function

class Utils {

	static val BASE_16 = "0123456789abcdef"

	/**
	 * Extension method to make option generation a bit prettier
	 */
	def static void buildOpt(Options it, String shortName, Consumer<Option.Builder> f) {
		val b = Option.builder(shortName)
		f.accept(b)
		addOption(b.build)
	}

	def static <T extends AutoCloseable, U> U with(T c, Function<T, U> f) {
		val r = f.apply(c)
		c.close()
		return r
	}

	/**
	 * Takes a colour id (see http://www.computercraft.info/wiki/Colors_(API)#Colors "Paint" column),
	 * and returns a {@link Color}.
	 */
	@Pure
	def static Color getCCColourFromInt(int i) {
		val col = Colour.fromInt(15 - i)
		return if(col == null) Color.WHITE else new Color(col.r, col.g, col.b)
	}

	@Pure
	def static Color getCCColourFromChar(char c) {
		return getCCColourFromInt(base16ToInt(c))
	}

	@Pure
	def static BufferedImage makeTintedCopy(BufferedImage it, Color tint) {
		val gc = GraphicsEnvironment.localGraphicsEnvironment.defaultScreenDevice.defaultConfiguration

		var tintedImg = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT)

		for (var y = 0; y < height; y++) {
			for (var x = 0; x < width; x++) {
				val rgb = getRGB(x, y)

				if (rgb != 0) {
					tintedImg.setRGB(x, y, tint.getRGB())
				}
			}
		}

		return tintedImg
	}

	@Pure
	def static base16ToInt(char c) {
		return BASE_16.indexOf(c.toString.toLowerCase)
	}
}
