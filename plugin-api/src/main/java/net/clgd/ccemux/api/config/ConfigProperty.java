package net.clgd.ccemux.api.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;

/**
 * An entry in a config file which stores a value of the given type.
 *
 * @param <T> The type of value to store.
 */
public class ConfigProperty<T> extends ConfigEntry {
	private final List<BiConsumer<T, T>> listeners = new ArrayList<>(0);

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
	public ConfigProperty<T> setName(@Nonnull String name) {
		this.name = name;
		return this;
	}

	private String description;

	@Nonnull
	public ConfigProperty<T> setDescription(@Nullable String description) {
		this.description = description;
		return this;
	}

	private final Type type;

	/**
	 * The type this property stores. This is likely to be a {@link Class} or
	 * {@link ParameterizedType}.
	 */
	@Nonnull
	public Type getType() {
		return type;
	}

	private final T defaultValue;

	/**
	 * The default value for this property.
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	private boolean alwaysEmit = false;

	/**
	 * Whether this property should always be written to a config file, irrespective
	 * of whether it's non-default.
	 *
	 * @see #isDefault()
	 */
	public boolean isAlwaysEmit() {
		return alwaysEmit;
	}

	/**
	 * Whether this property should always be written to a config file, irrespective
	 * of whether it's non-default.
	 *
	 * @see #isDefault()
	 */
	@Nonnull
	public ConfigProperty<T> setAlwaysEmit(boolean alwaysEmit) {
		this.alwaysEmit = alwaysEmit;
		return this;
	}

	private T value;

	private boolean isDefault;

	/**
	 * Whether this property has not been changed, and so stores the default value.
	 *
	 * Note that if a property has been changed and happens to have the same value
	 * as the default then this will be {@code false}. One must call
	 * {@link #resetDefault()} in order to mark it as default.
	 */
	public boolean isDefault() {
		return this.isDefault;
	}

	private ConfigProperty(String key, Type type, T defaultValue) {
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
	public ConfigProperty(@Nonnull String key, @Nonnull Class<T> type, T defaultValue) {
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
	public ConfigProperty(@Nonnull String key, @Nonnull TypeToken<T> type, T defaultValue) {
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
			T oldValue = value;
			value = newValue;

			for (BiConsumer<T, T> listener : listeners) {
				listener.accept(oldValue, newValue);
			}
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
	@Nonnull
	public Optional<String> getDescription() {
		return Optional.ofNullable(description);
	}

	/**
	 * Ensure this property is always written to a config file, irrespective of
	 * whether it's non-default.
	 *
	 * @return The current property.
	 * @see #isAlwaysEmit()
	 * @see #setAlwaysEmit(boolean)
	 */
	@Nonnull
	public ConfigProperty<T> setAlwaysEmit() {
		return setAlwaysEmit(true);
	}

	/**
	 * Add a listener which observes value changes.
	 *
	 * @param listener The event listener. This receives the old and new values as
	 *                 arguments.
	 * @see #addAndFireListener(BiConsumer)
	 * @see #removeListener(BiConsumer)
	 */
	public void addListener(@Nonnull BiConsumer<T, T> listener) {
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
	 * @param listener The event listener. This receives the old and new values as
	 *                 arguments.
	 * @see #addListener(BiConsumer)
	 * @see #removeListener(BiConsumer)
	 */
	public void addAndFireListener(@Nonnull BiConsumer<T, T> listener) {
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
	public void removeListener(@Nonnull BiConsumer<T, T> listener) {
		listeners.remove(listener);
	}
}
