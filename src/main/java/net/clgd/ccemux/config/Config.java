package net.clgd.ccemux.config;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.common.collect.*;
import com.google.gson.*;

import lombok.Value;

@Value
public class Config {
	@Value
	private class ChangeListener<T> {
		Class<T> cls;
		BiConsumer<T, T> listener;
	}

	private final Gson gson;

	/**
	 * Gets the underlying map of this config. Modifications to this map will
	 * affect the config, but will <b>not</b> fire listeners.
	 */
	private final Map<String, JsonElement> data;

	/**
	 * Gets the listeners for this config.<br>
	 * <br>
	 * Listeners contain a class representing the target value type, and a
	 * biconsumer which is passed the old value and new value as parameters.
	 */
	private final Multimap<String, ChangeListener<?>> listeners = Multimaps
			.synchronizedSetMultimap(MultimapBuilder.hashKeys().hashSetValues().build());

	@Value
	public class Property<T> {
		String key;
		Class<T> cls;

		public void addListener(BiConsumer<T, T> consumer) {
			Config.this.addListener(key, cls, consumer);
		}

		public void removeListener(BiConsumer<T, T> consumer) {
			Config.this.removeListener(key, consumer);
		}

		public Optional<T> get() {
			return getAs(key, cls);
		}

		public T getOr(T defaultValue) {
			return getAsOr(key, cls, defaultValue);
		}

		public void set(T value) {
			put(key, value);
		}
	}

	/**
	 * Adds a listener which is invoked whenever the given config property is
	 * changed.
	 * 
	 * @param cls
	 *            The type of value to be deserialized and passed to the
	 *            consumer.
	 * @param consumer
	 *            A consumer that takes the old and new values (respectively) of
	 *            the property that has changed.
	 */
	public <T> void addListener(String key, Class<T> cls, BiConsumer<T, T> consumer) {
		listeners.put(key, new ChangeListener<T>(cls, consumer));
	}

	public <T> void removeListener(String key, BiConsumer<T, T> consumer) {
		listeners.get(key).removeIf(c -> c.listener.equals(consumer));
	}

	public <T> void removeListener(BiConsumer<T, T> consumer) {
		listeners.values().removeIf(c -> c.listener.equals(consumer));
	}

	/**
	 * @return Whether the config contains a value for the given key.
	 */
	public boolean containsKey(String key) {
		return data.containsKey(key);
	}

	/**
	 * @return The raw {@link JsonElement} value at the given key, or an empty
	 *         <code>Optional</code> if no value is present for this key.
	 */
	public Optional<JsonElement> getRaw(String key) {
		return Optional.ofNullable(data.get(key));
	}

	/**
	 * @return The value for the given key, as a type specified by the
	 *         <code>cls</code> parameter.
	 */
	public <T> Optional<T> getAs(String key, Class<T> cls) throws JsonSyntaxException {
		return getRaw(key).map(j -> gson.fromJson(j, cls));
	}

	/**
	 * 
	 * @return The value for the given key, as a type specified by the
	 *         <code>cls</code> parameter, or the given default value if no
	 *         value is available for the given key.
	 */
	public <T> T getAsOr(String key, Class<T> cls, T defaultValue) {
		return getAs(key, cls).orElse(defaultValue);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void invokeListeners(String key, JsonElement from, JsonElement to) {
		listeners.get(key).stream().map(c -> (ChangeListener) c).forEach(c -> {
			c.listener.accept(gson.fromJson(from, c.cls), gson.fromJson(to, c.cls));
		});
	}

	/**
	 * Removes the value for the given key from this config.
	 */
	public void remove(String key) {
		JsonElement old = getRaw(key).orElse(null);
		data.remove(key);
		invokeListeners(key, old, null);
	}

	/**
	 * Adds a raw JSON element for the given key, overwriting any existing
	 * values.
	 */
	public void putRaw(String key, JsonElement e) {
		JsonElement old = getRaw(key).orElse(null);
		data.put(key, e);
		invokeListeners(key, old, e);
	}

	/**
	 * Adds a given element to the config for the given key, overwriting any
	 * existing values.
	 */
	public <T> void put(String key, T t) {
		putRaw(key, gson.toJsonTree(t));
	}

	public <T> Property<T> property(String key, Class<T> cls) {
		return new Property<>(key, cls);
	}
}
