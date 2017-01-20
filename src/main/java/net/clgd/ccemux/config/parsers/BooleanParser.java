package net.clgd.ccemux.config.parsers;

/**
 * A <code>Parser</code> implementation that produces <code>Boolean</code>
 * values. <code>"true"</code> will be parsed to <code>true</code>,
 * <code>"false"</code> to <code>false</code>, and any other input will cause a
 * <code>ParseException</code>.
 * 
 * @author apemanzilla
 *
 */
public class BooleanParser implements Parser<Boolean> {
	@Override
	public Boolean parse(String input) throws ParseException {
		if (input.equals("true"))
			return true;
		if (input.equals("false"))
			return false;
		throw new ParseException("Invalid boolean: " + input);
	}
}