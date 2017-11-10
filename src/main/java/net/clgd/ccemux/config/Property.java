package net.clgd.ccemux.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

/**
 * An entry in a config file which stores a value of the given type.
 *
 * @param <T> The type of value to store.
 */
@Accessors(chain = true)
public class Property<T> extends ConfigEntry {
	private final List<BiConsumer<T, T>> listeners = new ArrayList<>(0);

	@Getter
	private final String key;

	@Getter
	@Setter
	private String name;

	@Setter
	private String description;

	/**
	 * The type this property stores. This is likely to be a {@link Class} or
	 * {@link ParameterizedType}.
	 */
	@Getter
	private final Type type;

	/**
	 * The default value for this property.
	 */
	@Getter
	private final T defaultValue;

	/**
	 * Whether this property should always be written to a config file, irrespective
	 * of whether it's non-default.
	 *
	 * @see #isDefault()
	 */
	@Getter
	@Setter
	private boolean alwaysEmit = false;

	private T value;

	/**
	 * Whether this property has not been changed, and so stores the default value.
	 *
	 * Note that if a property has been changed and happens to have the same value
	 * as the default then this will be {@code false}. One must call {@link #resetDefault()}
	 * in order to mark it as default.
	 */
	@Getter
	private boolean isDefault;

	private Property(String key, Type type, T defaultValue) {
		this.key = key;
		this.name = key;
		this.type = type;

		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.isDefault = true;
	}

	/**
	 * Create a new property with the given key and add it to the group.
	 *
	 * @param key          The property's unique key.
	 * @param type         The type of this property's value.
	 * @param defaultValue The default value of this property.
	 * @throws IllegalStateException If an entry with the same key exists.
	 * @see Group#property(String, Class, Object)
	 */
	public Property(String key, Class<T> type, T defaultValue) {
		this(key, (Type) type, defaultValue);
	}

	/**
	 * Create a new property with the given key and add it to the group.
	 *
	 * @param key          The property's unique key.
	 * @param type         The type of this property's value.
	 * @param defaultValue The default value of this property.
	 * @throws IllegalStateException If an entry with the same key exists.
	 * @see Group#property(String, TypeToken, Object)
	 */
	public Property(String key, TypeToken<T> type, T defaultValue) {
		this(key, type.getType(), defaultValue);
	}

	/**
	 * Get the value for this property.
	 *
	 * @return This property's value.
	 */
	public T get() {
		return value;
	}

	/**
	 * Change the value of this property, marking it as dirty and firing listeners.
	 *
	 * @param newValue The value to change the property to.
	 * @see #resetDefault()
	 */
	public void set(T newValue) {
		set(newValue, false);
	}

	private void set(T newValue, boolean isDefault) {
		this.isDefault = isDefault;
		if (!newValue.equals(value)) {
			val oldValue = value;
			value = newValue;

			for (val listener : listeners) listener.accept(oldValue, newValue);
		}
	}

	/**
	 * Reset this property to the default value.
	 *
	 * @see #set(Object)
	 * @see #isDefault()
	 */
	public void resetDefault() {
		set(defaultValue, true);
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.ofNullable(description);
	}

	/**
	 * Ensure this property is always written to a config file, irrespective
	 * of whether it's non-default.
	 *
	 * @return The current property.
	 * @see #isAlwaysEmit()
	 * @see #setAlwaysEmit(boolean)
	 */
	public Property<T> setAlwaysEmit() {
		return setAlwaysEmit(true);
	}

	/**
	 * Add a listener which observes value changes.
	 *
	 * @param listener The event listener. This receives the old and new values as arguments.
	 * @see #addAndFireListener(BiConsumer)
	 * @see #removeListener(BiConsumer)
	 */
	public void addListener(BiConsumer<T, T> listener) {
		listeners.add(listener);
	}

	/**
	 * Add a property change listener and fire it in one go.
	 *
	 * This is convenient for syncing properties to an external field.
	 *
	 * <pre>
	 * {@code
	 * myProperty.addAndFireListener((a, b) -> CustomConfig.myField = b)
	 * }
	 * </pre>
	 *
	 * @param listener The event listener. This receives the old and new values as arguments.
	 * @see #addListener(BiConsumer)
	 * @see #removeListener(BiConsumer)
	 */
	public void addAndFireListener(BiConsumer<T, T> listener) {
		addListener(listener);
		listener.accept(value, value);
	}

	/**
	 * Remove a property change listener
	 *
	 * @param listener The event listener to remove.
	 * @see #addListener(BiConsumer)
	 * @see #addAndFireListener(BiConsumer)
	 */
	public void removeListener(BiConsumer<T, T> listener) {
		listeners.remove(listener);
	}
}
