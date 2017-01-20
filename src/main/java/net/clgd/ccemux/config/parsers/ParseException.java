package net.clgd.ccemux.config.parsers;

/**
 * Thrown when an exception occurs while parsing config options.
 * 
 * @author apemanzilla
 * @see net.clgd.ccemux.config.parsers.Parser Parser
 * @see net.clgd.ccemux.config.Config Config
 */
public class ParseException extends Exception {
	private static final long serialVersionUID = -2946287129442516098L;

	public ParseException() {
		super();
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
