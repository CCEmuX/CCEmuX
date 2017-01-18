package net.clgd.ccemux.config.parsers;

public class BooleanParser implements Parser<Boolean> {
	@Override
	public Boolean parse(String input) throws ParseException {
		if (input.equals("true")) return true;
		if (input.equals("false")) return false;
		throw new ParseException("Invalid boolean: " + input);
	}
}