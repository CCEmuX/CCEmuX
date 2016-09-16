package net.clgd.ccemux

import java.util.function.Consumer
import org.apache.commons.cli.Options
import org.apache.commons.cli.Option

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
	def static <T> T using(T t, Consumer<T> f) {
		f.accept(t)
		
		return t
	}
}