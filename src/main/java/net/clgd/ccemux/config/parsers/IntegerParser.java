package net.clgd.ccemux.config.parsers;

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