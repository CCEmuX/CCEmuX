package net.clgd.ccemux.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.clgd.ccemux.config.parsers.Parser;

/**
 * Denotes a field that should be loaded from or written to a configuration
 * storage using a given parser.
 * 
 * @author apemanzilla
 * @see net.clgd.ccemux.config.parsers.Parser Parser
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigOption {
	/**
	 * The key of the config option
	 */
	String key();

	/**
	 * The parser to use to convert string values into more useful values
	 */
	Class<Parser> parser();

	/**
	 * The default value to use in case it is undefined or invalid
	 */
	String defaultValue() default "";
}
