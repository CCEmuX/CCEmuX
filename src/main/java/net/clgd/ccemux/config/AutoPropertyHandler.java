package net.clgd.ccemux.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import lombok.Value;

/**
 * A {@link ConfigPropertyHandler} implementation that automatically chooses an
 * appropriate handler from an internal map. Handlers can be added via
 * {@link #addHandler(Class, ConfigPropertyHandler)}, which also takes a
 * {@link Class} object representing what class the handler can be applied
 * to.<br />
 * <br />
 * Handlers for primitives, boxed primitives, and <code>String</code> objects
 * are included by default.
 * 
 * @author apemanzilla
 *
 */
@Value
public class AutoPropertyHandler implements ConfigPropertyHandler<Object> {
	private static final Map<Class<?>, ConfigPropertyHandler<?>> handlers = new HashMap<>();

	public static <T> void addHandler(Class<T> cls, ConfigPropertyHandler<T> handler) {
		handlers.put(cls, handler);
	}

	public static <T> void addHandler(Class<T> cls, Function<T, String> serializer, Function<String, T> deserializer) {
		handlers.put(cls, new ConfigPropertyHandler<T>() {
			@Override
			public String serialize(T data) {
				return serializer.apply(data);
			}

			@Override
			public T deserialize(String serialized) {
				return deserializer.apply(serialized);
			}
		});
	}

	static {
		addHandler(Boolean.class, Object::toString, Boolean::parseBoolean);
		addHandler(boolean.class, Object::toString, Boolean::parseBoolean);
		
		addHandler(Byte.class, Object::toString, Byte::parseByte);
		addHandler(byte.class, Object::toString, Byte::parseByte);
		
		addHandler(Character.class, Object::toString, s -> s.charAt(0));
		addHandler(char.class, Object::toString, s -> s.charAt(0));

		addHandler(Double.class, Object::toString, Double::parseDouble);
		addHandler(double.class, Object::toString, Double::parseDouble);

		addHandler(Float.class, Object::toString, Float::parseFloat);
		addHandler(float.class, Object::toString, Float::parseFloat);

		addHandler(Integer.class, Object::toString, Integer::parseInt);
		addHandler(int.class, Object::toString, Integer::parseInt);

		addHandler(Long.class, Object::toString, Long::parseLong);
		addHandler(long.class, Object::toString, Long::parseLong);
		
		addHandler(Short.class, Object::toString, Short::parseShort);
		addHandler(short.class, Object::toString, Short::parseShort);

		addHandler(String.class, String::toString, String::new);
		
		addHandler(Void.class, v -> null, s -> null);
		addHandler(void.class, v -> null, s -> null);
	}

	private final Field target;

	public AutoPropertyHandler(Field target) {
		if (!handlers.containsKey(target.getType()))
			throw new IllegalStateException("No handler available for " + getTarget().getType().toString());

		this.target = target;
	}

	@SuppressWarnings("unchecked")
	private ConfigPropertyHandler<Object> getHandler() {
		return (ConfigPropertyHandler<Object>) handlers.get(getTarget().getType());
	}

	@Override
	public String serialize(Object data) {
		return getHandler().serialize(data);
	}

	@Override
	public Object deserialize(String serialized) {
		return getHandler().deserialize(serialized);
	}
}
