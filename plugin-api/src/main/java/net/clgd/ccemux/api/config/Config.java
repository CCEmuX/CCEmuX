package net.clgd.ccemux.api.config;

import javax.annotation.Nonnull;

import com.google.common.reflect.TypeToken;

/**
 * The base class for all configs, containing a root {@link Group} of entries.
 */
public class Config {
	private final Group root = new Group();

	/**
	 * The root group for the config. All properties and child groups are stored in
	 * this.
	 */
	@Nonnull
	public Group getRoot() {
		return root;
	}

	/**
	 * Create a new property with the given key and add it to the config file.
	 *
	 * @param key          The property's unique key.
	 * @param type         The type of this property's value.
	 * @param defaultValue The default value of this property.
	 * @return The newly created property.
	 * @throws IllegalStateException If an entry with the same key exists.
	 * @see ConfigProperty(String, TypeToken, Object)
	 */
	@Nonnull
	public <T> ConfigProperty<T> property(@Nonnull String key, @Nonnull TypeToken<T> type, @Nonnull T defaultValue) {
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
	 * @see ConfigProperty(String, Class, Object)
	 */
	@Nonnull
	public <T> ConfigProperty<T> property(@Nonnull String key, @Nonnull Class<T> type, @Nonnull T defaultValue) {
		return root.property(key, type, defaultValue);
	}

	/**
	 * Lookup a group with the given key, or create a new one.
	 *
	 * @param key The group's key.
	 * @return A group with the given key.
	 * @throws IllegalStateException If a _property_ with the same name exists.
	 */
	@Nonnull
	public Group group(@Nonnull String key) throws IllegalStateException {
		return root.group(key);
	}
}
