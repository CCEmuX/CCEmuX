package net.clgd.ccemux.config.parsers;

/**
 * A <code>Parser</code> implementation that produces <code>Double</code>
 * values. {@link java.lang.Double#parseDouble(String) Double.parseDouble} is
 * used, and <code>ParseExceptions</code> are thrown if a
 * <code>NumberFormatException</code> is caught.
 * 
 * @author apemanzilla
 *
 */
public class DoubleParser implements Parser<Double> {
	@Override
	public Double parse(String input) throws ParseException {
		try {
			return Double.parseDouble(input);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid double: '" + input + "'", e);
		}
	}
}