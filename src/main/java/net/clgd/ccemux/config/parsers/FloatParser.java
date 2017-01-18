package net.clgd.ccemux.config.parsers;

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