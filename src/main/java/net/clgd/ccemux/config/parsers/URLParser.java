package net.clgd.ccemux.config.parsers;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A <code>Parser</code> implementation that produces <code>URL</code> objects.
 * New <code>URL</code> objects are constructed from the input string, and
 * <code>ParseExceptions</code> are thrown if a
 * <code>MalformedURLException</code> is caught.
 * 
 * @author apemanzilla
 *
 */
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
