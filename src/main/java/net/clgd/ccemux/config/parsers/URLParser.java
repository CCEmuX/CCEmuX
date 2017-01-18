package net.clgd.ccemux.config.parsers;

import java.net.MalformedURLException;
import java.net.URL;

public class URLParser implements Parser<URL> {
	@Override
	public URL parse(String input) throws ParseException {
		try {
			return new URL(input);
		} catch (MalformedURLException e) {
			throw new ParseException("Invalid URL: '" + input + "'", e);
		}
	}
}
