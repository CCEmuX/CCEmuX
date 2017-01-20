package net.clgd.ccemux.config.parsers;

/**
 * A <code>Parser</code> implementation that produces <code>Long</code> values.
 * {@link java.lang.Long#parseLong(String) Long.parseLong} is used, and
 * <code>ParseExceptions</code> are thrown if a
 * <code>NumberFormatException</code> is caught.
 * 
 * @author apemanzilla
 *
 */
public class LongParser implements Parser<Long> {
	@Override
	public Long parse(String input) throws ParseException {
		try {
			return Long.parseLong(input);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid long: '" + input + "'", e);
		}
	}
}