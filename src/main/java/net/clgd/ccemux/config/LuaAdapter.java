package net.clgd.ccemux.config;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;

import javax.annotation.Nonnull;

import com.google.common.primitives.Primitives;
import com.google.gson.reflect.TypeToken;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import net.clgd.ccemux.api.config.ConfigEntry;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;

/**
 * An adapter for ComputerCraft's Lua objects, which attempts to convert between
 * config groups/properties and CC's HashMap + primitives.
 */
public final class LuaAdapter {
	private LuaAdapter() {}

	@SuppressWarnings("unchecked")
	public static Map<?, ?> toLua(Group group, Object existing) {
		Map<Object, Object> object = existing instanceof Map ? (Map<Object, Object>) existing : new HashMap<>();

		for (ConfigEntry entry : group.children()) {
			if (entry instanceof ConfigProperty<?>) {
				ConfigProperty<?> property = (ConfigProperty<?>) entry;

				// Don't emit default options
				if (!property.isDefault() || property.isAlwaysEmit()) {
					object.put(entry.getKey(), toLua(property.get()));
				}
			} else if (entry instanceof Group) {
				Map<?, ?> element = toLua((Group) entry, object.get(entry.getKey()));

				// Don't emit empty groups
				if (element.size() > 0) object.put(entry.getKey(), element);
			}
		}

		return object;
	}

	public static Map<?, ?> toDefaultJson(Group group) {
		Map<String, Object> object = new HashMap<>();
		for (ConfigEntry entry : group.children()) {
			if (entry instanceof ConfigProperty<?>) {
				ConfigProperty<?> property = (ConfigProperty<?>) entry;
				object.put(entry.getKey(), toLua(property.getDefaultValue()));
			} else if (entry instanceof Group) {
				object.put(entry.getKey(), toDefaultJson((Group) entry));
			}
		}

		return object;
	}

	@SuppressWarnings("unchecked")
	public static void fromLua(Group group, Object element) throws LuaException {
		if (!(element instanceof Map)) {
			throw new LuaException(String.format("bad key '%s' for property group (table expected, got %s)",
				group.getKey(),
				LuaValues.getType(element)));
		}

		for (Map.Entry<?, ?> entry : ((Map<?, ?>) element).entrySet()) {
			if (!(entry.getKey() instanceof String)) {
				throw new LuaException(String.format("unexpected key of type '%s' in group '%s'",
					LuaValues.getType(entry.getKey()), group.getKey()));
			}

			String key = (String) entry.getKey();
			if (key.startsWith("_")) continue;

			Optional<ConfigEntry> configEntry = group.child(key);
			if (configEntry.isPresent()) {
				if (configEntry.get() instanceof Group) {
					fromLua((Group) configEntry.get(), entry.getValue());
				} else if (configEntry.get() instanceof ConfigProperty<?>) {
					ConfigProperty<Object> property = (ConfigProperty<Object>) configEntry.get();

					try {
						property.set(fromValue(entry.getValue(), property.getType()));
					} catch (IllegalArgumentException e) {
						throw new LuaException(String.format(
							"Cannot parse property '%s' in group '%s' (%s)",
							entry.getKey(), group.getKey(), e.getMessage())
						);
					}
				}
			} else {
				throw new LuaException(String.format("Unknown property '%s' in group '%s'", key, group.getKey()));
			}
		}
	}

	private static Object toLua(Object input) {
		if (input instanceof Collection<?>) {
			int i = 0;
			HashMap<Integer, Object> result = new HashMap<>();
			for (Object value : (Collection<?>) input) result.put(i++, toLua(value));
			return result;
		} else if (input.getClass().isArray()) {
			int i = 0;
			HashMap<Integer, Object> result = new HashMap<>();
			for (Object value : (Object[]) input) result.put(i++, toLua(value));
			return result;
		} else {
			return input;
		}
	}

	private static Object fromValue(Object input, Type type) throws IllegalArgumentException {
		TypeToken<?> typeToken = TypeToken.get(type);
		Class<?> klass = typeToken.getRawType();
		if (Primitives.isWrapperType(klass)) klass = Primitives.unwrap(klass);

		if (klass == int.class) {
			if (input instanceof Number) return ((Number) input).intValue();
		} else if (klass == long.class) {
			if (input instanceof Number) return ((Number) input).longValue();
		} else if (klass == double.class) {
			if (input instanceof Number) return ((Number) input).doubleValue();
		} else if (klass == boolean.class) {
			if (input instanceof Boolean) return input;
		} else if (klass == String.class) {
			if (input instanceof String) return input;
		} else if (klass.isArray()) {
			if (input instanceof Map) {
				Map<Object, Object> map = new HashMap<>((Map<?, ?>) input);

				int maxKey = 0;
				while (map.containsKey(maxKey + 1)) maxKey++;

				Object array = Array.newInstance(klass.getComponentType(), maxKey);
				for (int i = 0; i <= maxKey; i++) {
					Array.set(array, i, fromValue(map.remove(i + 1), klass.getComponentType()));
				}

				Iterator<Object> keys = map.keySet().iterator();
				if (keys.hasNext()) {
					throw new IllegalArgumentException(String.format("Unexpected key of type '%s'",
						LuaValues.getType(keys.next())));
				}
				return array;
			}
		}

		throw new IllegalArgumentException(String.format(
			"%s expected, got %s",
			getType(klass), LuaValues.getType(input)));
	}


	@Nonnull
	private static String getType(Class<?> type) {
		if (type == null) {
			return "nil";
		} else if (type == String.class) {
			return "string";
		} else if (type == boolean.class) {
			return "boolean";
		} else if (type == double.class || type == long.class || type == int.class) {
			return "number";
		} else if (Map.class.isAssignableFrom(type)) {
			return "table";
		} else {
			if (!type.isArray()) {
				return type.getName();
			} else {
				StringBuilder name;
				for (name = new StringBuilder(); type.isArray(); type = type.getComponentType()) {
					name.append("[]");
				}

				name.insert(0, type.getName());
				return name.toString();
			}
		}
	}
}
