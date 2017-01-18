package net.clgd.ccemux.config.parsers;

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