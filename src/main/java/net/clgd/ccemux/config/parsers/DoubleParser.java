package net.clgd.ccemux.config.parsers;

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