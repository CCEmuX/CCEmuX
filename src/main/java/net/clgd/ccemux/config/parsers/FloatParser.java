package net.clgd.ccemux.config.parsers;

/**
 * A <code>Parser</code> implementation that produces <code>Float</code> values.
 * {@link java.lang.Float#parseFloat(String) Float.parseFloat} is used, and
 * <code>ParseExceptions</code> are thrown if a
 * <code>NumberFormatException</code> is caught.
 * 
 * @author apemanzilla
 *
 */
public class FloatParser implements Parser<Float> {
	@Override
	public Float parse(String input) throws ParseException {
		try {
			return Float.parseFloat(input);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid float: '" + input + "'", e);
		}
	}
}