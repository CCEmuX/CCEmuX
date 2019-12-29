package net.clgd.ccemux.api.config;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;

/**
 * A group acts as a collection of child properties and groups, allowing
 * for greater organisation of config files.
 *
 * @see ConfigEntry
 * @see ConfigProperty
 */
public class Group extends ConfigEntry {
	private final String key;

	@Override
	@Nonnull
	public String getKey() {
		return key;
	}

	private String name;

	@Override
	@Nonnull
	public String getName() {
		return name;
	}

	@Nonnull
	public Group setName(@Nonnull String name) {
		this.name = name;
		return this;
	}

	private String description;

	@Nonnull
	public Group setDescription(@Nullable String description) {
		this.description = description;
		return this;
	}

	private final Map<String, ConfigEntry> children = new LinkedHashMap<>();

	public Group(@Nonnull String key) {
		this.key = key;
		this.name = key;
	}

	Group() {
		this("");
	}

	/**
	 * Add a new property to this group.
	 *
	 * @param property The property to add.
	 * @return The current group ({@code this}).
	 * @throws IllegalStateException If an entry with the same key already exists.
	 */
	@Nonnull
	public Group addProperty(@Nonnull ConfigProperty<?> property) {
		if (children.containsKey(property.getKey())) {
			throw new IllegalStateException("Already an entry with the given key");
		}
		children.put(property.getKey(), property);
		return this;
	}

	/**
	 * Add a new child group to this group.
	 *
	 * @param group The group to add.
	 * @return The current group ({@code this}).
	 * @throws IllegalStateException If an entry with the same key already exists.
	 */
	@Nonnull
	public Group addGroup(@Nonnull Group group) {
		if (children.containsKey(group.getKey())) {
			throw new IllegalStateException("Already an entry with the given key");
		}
		children.put(group.getKey(), group);
		return this;
	}

	/**
	 * Get all children in this group.
	 *
	 * @return An unmodifiable collection of children.
	 */
	@Nonnull
	public Collection<ConfigEntry> children() {
		return Collections.unmodifiableCollection(children.values());
	}

	/**
	 * Get a child with the given key.
	 *
	 * @param key The key for the corresponding child.
	 * @return The child entry if it exists.
	 */
	@Nonnull
	public Optional<ConfigEntry> child(@Nonnull String key) {
		return Optional.ofNullable(children.get(key));
	}

	@Override
	@Nonnull
	public Optional<String> getDescription() {
		return Optional.ofNullable(description);
	}

	/**
	 * Create a new property with the given key and add it to the group.
	 *
	 * @param key          The property's unique key.
	 * @param type         The type of this property's value.
	 * @param defaultValue The default value of this property.
	 * @return The newly created property.
	 * @throws IllegalStateException If an entry with the same key exists.
	 * @see ConfigProperty(String, TypeToken, Object)
	 */
	@Nonnull
	public <T> ConfigProperty<T> property(@Nonnull String key, @Nonnull TypeToken<T> type, T defaultValue) {
		ConfigProperty<T> property = new ConfigProperty<>(key, type, defaultValue);
		addProperty(property);
		return property;
	}

	/**
	 * Create a new property with the given key and add it to the group.
	 *
	 * @param key          The property's unique key.
	 * @param type         The type of this property's value.
	 * @param defaultValue The default value of this property.
	 * @return The newly created property.
	 * @throws IllegalStateException If an entry with the same key exists.
	 * @see ConfigProperty(String, Class, Object)
	 */
	@Nonnull
	public <T> ConfigProperty<T> property(@Nonnull String key, @Nonnull Class<T> type, T defaultValue) {
		ConfigProperty<T> property = new ConfigProperty<>(key, type, defaultValue);
		addProperty(property);
		return property;
	}

	/**
	 * Lookup a group with the given key, or create a new one.
	 *
	 * @param key The group's key.
	 * @return A group with the given key.
	 * @throws IllegalStateException If a _property_ with the same name exists.
	 */
	@Nonnull
	public Group group(@Nonnull String key) {
		final net.clgd.ccemux.api.config.ConfigEntry entry = children.get(key);
		if (entry == null) {
			final net.clgd.ccemux.api.config.Group group = new Group(key);
			addGroup(group);
			return group;
		} else if (entry instanceof Group) {
			return (Group) entry;
		} else {
			throw new IllegalStateException("A property exists with the same key");
		}
	}
}
