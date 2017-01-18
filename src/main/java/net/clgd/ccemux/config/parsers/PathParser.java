package net.clgd.ccemux.config.parsers;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
