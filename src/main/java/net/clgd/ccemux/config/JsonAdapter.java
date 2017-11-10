package net.clgd.ccemux.config;

import java.util.Map;
import java.util.Optional;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;

/**
 * A serialiser for {@link Config} instances which converts them to and from
 * {@link JsonElement}s.
 */
@Slf4j
public class JsonAdapter {
	private static final JsonAdapter instance = new JsonAdapter(
			new GsonBuilder()
					.setPrettyPrinting()
					.setLenient()
					.create()
	);

	public static JsonAdapter instance() {
		return instance;
	}

	private final Gson gson;

	/**
	 * Construct a new serialiser with the given {@link Gson} instance.
	 *
	 * One may wish to register custom strategies for various property types.
	 *
	 * @param gson The finalised instance to use.
	 */
	public JsonAdapter(Gson gson) {
		this.gson = gson;
	}

	/**
	 * Convert a config specification to JSON
	 *
	 * @param config The specification to convert.
	 * @return The converted object.
	 */
	public JsonElement toJson(Config config) {
		return toJson(config.getRoot());
	}

	/**
	 * Load configuration values from JSON
	 *
	 * @param config  The config to load to
	 * @param element The element to load from.
	 */
	public void fromJson(Config config, JsonElement element) {
		fromJson(config.getRoot(), element);
	}

	private JsonObject toJson(Group group) {
		JsonObject object = new JsonObject();
		for (ConfigEntry entry : group.children()) {
			if (entry instanceof Property<?>) {
				Property<?> property = (Property<?>) entry;

				// Don't emit default options
				if (!property.isDefault() || property.isAlwaysEmit()) {
					object.add(entry.getKey(), gson.toJsonTree(property.get()));
				}
			} else if (entry instanceof Group) {
				JsonObject element = toJson((Group) entry);

				// Don't emit empty groups
				if (element.size() > 0) object.add(entry.getKey(), element);
			}
		}

		return object;
	}

	private void fromJson(Group group, JsonElement element) {
		if (!(element instanceof JsonObject)) {
			log.error("Expected object for property group {}, got {}", group.getKey(), element);
			return;
		}

		JsonObject object = (JsonObject) element;
		for (Map.Entry<String, JsonElement> jsonEntry : object.entrySet()) {
			Optional<ConfigEntry> configEntry = group.child(jsonEntry.getKey());
			if (configEntry.isPresent()) {
				if (configEntry.get() instanceof Group) {
					fromJson((Group) configEntry.get(), jsonEntry.getValue());
				} else if (configEntry.get() instanceof Property<?>) {
					Property property = (Property<?>) configEntry.get();

					try {
						//noinspection unchecked
						property.set(gson.fromJson(jsonEntry.getValue(), property.getType()));
					} catch (JsonSyntaxException e) {
						log.error("Cannot parse property '{}' in group '{}' ({})", jsonEntry.getKey(), group.getKey(), e.getMessage());
					}
				}
			} else {
				log.error("Unknown property '{}' in group '{}'", jsonEntry.getKey(), group.getKey());
			}
		}
	}
}
