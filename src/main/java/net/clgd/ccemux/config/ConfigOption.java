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
	public String key();

	/**
	 * The parser to use to convert string values into more useful values
	 */
	public Class<? extends Parser<?>> parser();

	/**
	 * The default value to use in case it is left blank, or when generating a
	 * config file
	 */
	public String defaultValue();

	/**
	 * When writing to a config file, options with the same group attribute will
	 * be grouped together within the file under a comment with the group
	 * name.<br/>
	 * <br/>
	 * Defaults to an empty string, which does not perform any special grouping.
	 * 
	 * @return
	 */
	public String group() default "";

	/**
	 * The description for this config option. Used when generating comments for
	 * config files.<br/>
	 * <br/>
	 * Defaults to an empty string, which does not 
	 */
	public String description() default "";
}
