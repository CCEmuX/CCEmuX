package net.clgd.ccemux.config.parsers;

public class BooleanParser implements Parser<Boolean> {
	@Override
	public Boolean parse(String input) {
		return Boolean.parseBoolean(input);
	}
}