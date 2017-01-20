package net.clgd.ccemux.config.parsers;

/**
 * A <code>Parser</code> implementation that produces <code>Integer</code>
 * values. {@link java.lang.Integer#parseInteger(String) Integer.parseInteger}
 * is used, and <code>ParseExceptions</code> are thrown if a
 * <code>NumberFormatException</code> is caught.
 * 
 * @author apemanzilla
 *
 */
public class IntegerParser implements Parser<Integer> {
	@Override
	public Integer parse(String input) throws ParseException {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid integer: '" + input + "'", e);
		}
	}
}