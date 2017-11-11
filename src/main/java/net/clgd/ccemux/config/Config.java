package net.clgd.ccemux.config;

import com.google.gson.reflect.TypeToken;
import lombok.Getter;

/**
 * The base class for all config files.
 */
public class Config {
	/**
	 * The root group for the config. All properties and child groups
	 * are stored in this.
	 */
	@Getter
	private final Group root = new Group();


	/**
	 * Create a new property with the given key and add it to the config file.
	 *
	 * @param key          The property's unique key.
	 * @param type         The type of this property's value.
	 * @param defaultValue The default value of this property.
	 * @return The newly created property.
	 * @throws IllegalStateException If an entry with the same key exists.
	 * @see Property#Property(String, TypeToken, Object)
	 */
	public <T> Property<T> property(String key, TypeToken<T> type, T defaultValue) {
		return root.property(key, type, defaultValue);
	}

	/**
	 * Create a new property with the given key and add it to the config file.
	 *
	 * @param key          The property's unique key.
	 * @param type         The type of this property's value.
	 * @param defaultValue The default value of this property.
	 * @return The newly created property.
	 * @throws IllegalStateException If an entry with the same key exists.
	 * @see Property#Property(String, Class, Object)
	 */
	public <T> Property<T> property(String key, Class<T> type, T defaultValue) {
		return root.property(key, type, defaultValue);
	}

	/**
	 * Lookup a group with the given key, or create a new one.
	 *
	 * @param key The group's key.
	 * @return A group with the given key.
	 * @throws IllegalStateException If a _property_ with the same name exists.
	 */
	public Group group(String key) {
		return root.group(key);
	}
}
