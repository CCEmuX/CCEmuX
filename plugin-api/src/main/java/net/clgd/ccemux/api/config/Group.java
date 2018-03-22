package net.clgd.ccemux.api.config;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.reflect.TypeToken;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;

/**
 * A group acts as a collection of child properties and groups, allowing
 * for greater organisation of config files.
 *
 * @see ConfigEntry
 * @see ConfigProperty
 */
@Accessors(chain = true)
public class Group extends ConfigEntry {
	@Getter
	private final String key;

	@Getter
	@Setter
	private String name;

	@Setter
	private String description;

	private final Map<String, ConfigEntry> children = new LinkedHashMap<>();

	public Group(String key) {
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
	public Group addProperty(ConfigProperty<?> property) {
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
	public Group addGroup(Group group) {
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
	public Collection<ConfigEntry> children() {
		return Collections.unmodifiableCollection(children.values());
	}

	/**
	 * Get a child with the given key.
	 *
	 * @param key The key for the corresponding child.
	 * @return The child entry if it exists.
	 */
	public Optional<ConfigEntry> child(String key) {
		return Optional.ofNullable(children.get(key));
	}

	@Override
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
	 * @see ConfigProperty#Property(String, TypeToken, Object)
	 */
	public <T> ConfigProperty<T> property(String key, TypeToken<T> type, T defaultValue) {
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
	 * @see ConfigProperty#Property(String, Class, Object)
	 */
	public <T> ConfigProperty<T> property(String key, Class<T> type, T defaultValue) {
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
	public Group group(String key) {
		val entry = children.get(key);
		if (entry == null) {
			val group = new Group(key);
			addGroup(group);
			return group;
		} else if (entry instanceof Group) {
			return (Group) entry;
		} else {
			throw new IllegalStateException("A property exists with the same key");
		}
	}
}
