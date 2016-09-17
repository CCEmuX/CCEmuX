package net.clgd.ccemux

import dan200.computercraft.shared.util.Colour
import java.awt.Color
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
	 * Takes a colour id (see http://www.computercraft.info/wiki/Colors_(API)#Colors "Paint" column),
	 * and returns a {@link java.awt.Color}.
	 */
	@Pure
	def static Color getCCColourFromInt(int i) {
		val col = Colour.fromInt(15 - i)
		return 	if (col == null) Color.WHITE 
				else new Color(col.r, col.g, col.b)
	}
}