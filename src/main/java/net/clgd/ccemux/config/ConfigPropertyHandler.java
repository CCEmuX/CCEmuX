package net.clgd.ccemux.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.common.base.MoreObjects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.SneakyThrows;
import lombok.val;
import net.clgd.ccemux.Utils;

/**
 * Used to customize config de/serialization behavior. To be used, a constructor
 * with either no arguments or a single {@link Field} argument must be present.
 * It is recommended to use a constructor taking a <code>Field</code> parameter,
 * so that you can throw an exception with a helpful message if the field's type
 * is incompatible rather than waiting for a {@link ClassCastException} to be
 * thrown later.
 * 
 * @author apemanzilla
 *
 * @param <T>
 *            The type this handler can be applied to
 */
public interface ConfigPropertyHandler<T> {
	/**
	 * Reflectively applies a config (in the form of a
	 * <code>Map<String, String></code>) to the given object, returning the
	 * object. Non-static fields (of all accessibility) annotated with
	 * {@link ConfigProperty} will be set to the value present in the map (or
	 * unchanged if there is no value in the map), while unannotated fields will
	 * be left alone.
	 * 
	 * @param object
	 *            The object to apply the values to
	 * @param values
	 *            The map of values to be applied
	 * @return The same object that was passed
	 * 
	 * @see ConfigProperty
	 * @see #getConfig(Object)
	 */
	public static <T> T apply(T object, Map<String, String> values) throws IllegalAccessException {
		for (Field f : object.getClass().getDeclaredFields()) {
			f.setAccessible(true);

			if (!Modifier.isStatic(f.getModifiers()) && values.containsKey(f.getName())
					&& f.isAnnotationPresent(ConfigProperty.class)) {
				val handler = HandlerCache.getHandler(f);
				if (handler != null) f.set(object, handler.deserialize(values.get(f.getName())));
			}
		}

		return object;
	}

	/**
	 * Reflectively gets a <code>Map<String, String></code> of serialized values
	 * from the given object, according to {@link ConfigProperty}
	 * annotations.<br />
	 * <br />
	 * Values will not be added to the map if any of the following conditions
	 * are met:
	 * <ul>
	 * <li>The field is not annotated with {@link ConfigProperty}</li>
	 * <li>The field is static</li>
	 * <li>The string returned by the {@link ConfigPropertyHandler} is
	 * <code>null</code></li>
	 * </ul>
	 * 
	 * @param object
	 * @return
	 * @throws IllegalAccessException
	 */
	public static Map<String, String> getConfig(Object object) throws IllegalAccessException {
		val values = new HashMap<String, String>();

		for (Field f : object.getClass().getDeclaredFields()) {
			f.setAccessible(true);

			if (!Modifier.isStatic(f.getModifiers()) && f.isAnnotationPresent(ConfigProperty.class)) {
				@SuppressWarnings("rawtypes")
				ConfigPropertyHandler handler = HandlerCache.getHandler(f);
				@SuppressWarnings("unchecked")
				String got = handler.serialize(f.get(object));
				if (got != null) values.put(f.getName(), got);
			}
		}

		return values;
	}

	public String serialize(T data);

	public T deserialize(String serialized);
}

/**
 * A helper class for caching handlers
 * 
 * @author apemanzilla
 */
class HandlerCache {
	static Cache<Field, ConfigPropertyHandler<?>> cache = CacheBuilder.newBuilder().maximumSize(100).build();

	@SneakyThrows(ExecutionException.class)
	static ConfigPropertyHandler<?> getHandler(Field f) {
		val prop = f.getAnnotation(ConfigProperty.class);
		if (prop == null) return null;

		return cache.get(f, () -> {
			val hc = prop.handler();
			val c = MoreObjects.firstNonNull(Utils.tryGet(() -> hc.getConstructor(Field.class)),
					Utils.tryGet(hc::getConstructor));

			c.setAccessible(true);
			return c.newInstance((Object[]) Arrays.copyOf(new Field[] { f }, c.getParameterCount()));
		});

	}

	private HandlerCache() {}
}