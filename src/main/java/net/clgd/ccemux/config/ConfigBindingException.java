package net.clgd.ccemux.config;

/**
 * Thrown when a reflection-related exception occurs while binding options to a config object
 * @author apemanzilla
 */
public class ConfigBindingException extends RuntimeException {
	private static final long serialVersionUID = 4246045462350764728L;
	
	public ConfigBindingException() {
		super();
	}

	public ConfigBindingException(String message) {
		super(message);
	}

	public ConfigBindingException(Throwable cause) {
		super(cause);
	}

	public ConfigBindingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigBindingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
