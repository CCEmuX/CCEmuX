package net.clgd.ccemux.config;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.clgd.ccemux.config.parsers.ParseException;
import net.clgd.ccemux.config.parsers.Parser;

/**
 * Represents a config that is parsed from a key-value map using
 * {@link net.clgd.ccemux.config.ConfigOption ConfigOption}. Fields annotated
 * with <code>@ConfigOption</code> will be reflectively set using the given
 * parsers when {@link #loadConfig(Map)} is called.<br />
 * <br />
 * You can actually call #loadConfig(Map)
 * 
 * @author apemanzilla
 *
 */
public interface Config {
	/**
	 * Parses and sets fields in this <code>Config</code> object from a provided
	 * <code>Map</code> object using data provided by
	 * {@link net.clgd.ccemux.config.ConfigOption ConfigOption} annotations.
	 * 
	 * @param map
	 *            The key-value map of options that will be bound to this
	 *            object's fields
	 * @throws ParseException
	 *             Thrown when any other the parsers fail with a
	 *             <code>ParseException</code>, usually due to invalid data
	 *             format
	 * @throws ConfigBindingException
	 *             Thrown when an unexpected reflection exception is thrown.
	 *             This should not happen with normal use.
	 */
	public default void bindConfigOptions(Map<String, String> map) throws ParseException, ConfigBindingException {
		List<Field> configFields = Arrays.stream(getClass().getDeclaredFields())
				.filter(f -> f.getDeclaredAnnotation(ConfigOption.class) != null).collect(Collectors.toList());

		for (Field f : configFields) {
			ConfigOption opt = f.getDeclaredAnnotation(ConfigOption.class);
			Class<? extends Parser<?>> c = opt.parser();
			Parser<?> p;

			try {
				p = c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ConfigBindingException("Failed to instantiate parser " + c.getSimpleName(), e);
			}

			try {
				if (!f.isAccessible())
					f.setAccessible(true);
				f.set(this, p.parse(map.getOrDefault(opt.key(), opt.defaultValue())));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ConfigBindingException(
						"Failed to set field " + f.getName() + " while binding class " + c.getSimpleName(), e);
			}
		}
	}
}
