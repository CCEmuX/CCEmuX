package net.clgd.ccemux

import dan200.computercraft.shared.util.Colour
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.util.function.Consumer
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

class Utils {
	/**
	 * Extension method to make option generation a bit prettier
	 */
	def static void buildOpt(Options it, String shortName, Consumer<Option.Builder> f) {
		val b = Option.builder(shortName)
		f.accept(b)
		addOption(b.build)
	}
	
	/**
	 * Method to make initialization of objects easier
	 */
 	@Pure
	def static <T> T using(T t, Consumer<T> f) {
		f.accept(t)
		
		return t
	}
	
	/** 
	 * Safely runs a lambda expression on a value, catching and ignoring any thrown exception.
	 */
	@Pure
	def static <T> void tryWith(T t, Consumer<T> f) {
		try {
			f.accept(t)
		} catch (Exception e) {}
	}
	
	/**
	 * Takes a colour id (see http://www.computercraft.info/wiki/Colors_(API)#Colors "Paint" column),
	 * and returns a {@link Color}.
	 */
	@Pure
	def static Color getCCColourFromInt(int i) {
		val col = Colour.fromInt(15 - i)
		return 	if (col == null) Color.WHITE 
				else new Color(col.r, col.g, col.b)
	}
	
	def static Color getCCColourFromChar(char c) {
		return getCCColourFromInt(base16ToInt(c))
	}
	
	def static BufferedImage makeTintedCopy(BufferedImage it, Color tint) {
		val gc = GraphicsEnvironment.localGraphicsEnvironment
									.defaultScreenDevice
									.defaultConfiguration
		
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
	
	static val BASE_16 = "0123456789abcdef"
	
	def static base16ToInt(char c) {
		return BASE_16.indexOf(c.toString.toLowerCase)
	}
}