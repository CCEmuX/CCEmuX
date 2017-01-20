package net.clgd.ccemux.config.parsers;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * * A <code>Parser</code> implementation that produces <code>Path</code>
 * objects. {@link java.nio.file.Paths#get(String, String...) Paths.get(String,
 * String...)} is used, and <code>ParseExceptions</code> are thrown if an
 * <code>InvalidPathException</code> is caught.
 * 
 * @author apemanzilla
 *
 */
public class PathParser implements Parser<Path> {
	@Override
	public Path parse(String input) throws ParseException {
		try {
			return Paths.get(input);
		} catch (InvalidPathException e) {
			throw new ParseException("Invalid path: '" + input + "'", e);
		}
	}
}
