package net.clgd.ccemux.config.parsers;

/**
 * A <code>Parser</code> is a functional interface that can be used to parse a
 * <code>String</code> input to a more useful object. It is intended to be used
 * for parsing config options, but may be useful for other things too.
 * 
 * @author apemanzilla
 *
 * @param <T>
 *            The type of the object that this parser creates.
 * @see #parse(String)
 * @see net.clgd.ccemux.config.parsers.ParseException ParseException
 * @see net.clgd.ccemux.config.ConfigOption ConfigOption
 */
@FunctionalInterface
public interface Parser<T> {
	/**
	 * Parses an input string to an object of a given type.
	 * 
	 * @param input
	 *            The input string
	 * @return The object
	 * @throws ParseException
	 *             Thrown if an exception occurs - i.e. invalid number format
	 *             when parsing strings to integers.
	 */
	public T parse(String input) throws ParseException;
}
