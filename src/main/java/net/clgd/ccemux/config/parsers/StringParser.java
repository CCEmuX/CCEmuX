package net.clgd.ccemux.config.parsers;

/**
 * * A <code>Parser</code> implementation that produces <code>String</code>
 * objects. The input string is returned directly and a
 * <code>ParseException</code> will never be thrown.
 * 
 * @author apemanzilla
 *
 */
public class StringParser implements Parser<String> {
	@Override
	public String parse(String input) {
		return input;
	}
}