package net.clgd.ccemux.config;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;
import net.clgd.ccemux.api.config.Config;
import net.clgd.ccemux.api.config.ConfigEntry;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;

/**
 * A serialiser for {@link Config} instances which converts them to and from
 * {@link JsonElement}s.
 */
public class JsonAdapter {
	private static final Logger log = LoggerFactory.getLogger(JsonAdapter.class);

	private static final Gson defaultGson = new GsonBuilder()
		.setPrettyPrinting()
		.setLenient()
		.create();

	private final Gson gson;
	private final Config config;
	private JsonElement backing = new JsonObject();

	/**
	 * Construct a new serialiser/deserialiser for a config file with the given
	 * {@link Gson} instance.
	 *
	 * One may wish to register custom strategies for various property types.
	 *
	 * @param gson   The finalised instance to use.
	 * @param config The config file to interact with.
	 */
	public JsonAdapter(Gson gson, Config config) {
		this.gson = gson;
		this.config = config;
	}

	/**
	 * Construct a new serialiser/deserialiser for a config file with the default
	 * {@link Gson} instance.
	 *
	 * @param config The config file to interact with.
	 */
	public JsonAdapter(Config config) {
		this(defaultGson, config);
	}

	/**
	 * Convert a config specification to JSON
	 *
	 * @return The converted object.
	 */
	public JsonElement toJson() {
		return backing = toJson(config.getRoot(), backing);
	}

	/**
	 * Convert the default values to JSON.
	 *
	 * @return The default values in JSON.
	 */
	public JsonElement toDefaultJson() {
		return toDefaultJson(config.getRoot());
	}

	/**
	 * Load configuration values from JSON
	 *
	 * @param element The element to load from.
	 */
	public void fromJson(JsonElement element) {
		fromJson(config.getRoot(), backing = element);
	}

	private JsonObject toJson(Group group, JsonElement existing) {
		JsonObject object = existing instanceof JsonObject ? (JsonObject) existing : new JsonObject();
		for (ConfigEntry entry : group.children()) {
			if (entry instanceof ConfigProperty<?>) {
				ConfigProperty<?> property = (ConfigProperty<?>) entry;

				// Don't emit default options
				if (!property.isDefault() || property.isAlwaysEmit()) {
					object.add(entry.getKey(), gson.toJsonTree(property.get()));
				}
			} else if (entry instanceof Group) {
				JsonObject element = toJson((Group) entry, object.get(entry.getKey()));

				// Don't emit empty groups
				if (element.size() > 0) object.add(entry.getKey(), element);
			}
		}

		return object;
	}

	private JsonObject toDefaultJson(Group group) {
		JsonObject object = new JsonObject();
		for (ConfigEntry entry : group.children()) {
			if (entry instanceof ConfigProperty<?>) {
				ConfigProperty<?> property = (ConfigProperty<?>) entry;
				object.add(entry.getKey(), gson.toJsonTree(property.getDefaultValue()));
			} else if (entry instanceof Group) {
				object.add(entry.getKey(), toDefaultJson((Group) entry));
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
			if (jsonEntry.getKey().startsWith("_")) continue;

			Optional<ConfigEntry> configEntry = group.child(jsonEntry.getKey());
			if (configEntry.isPresent()) {
				if (configEntry.get() instanceof Group) {
					fromJson((Group) configEntry.get(), jsonEntry.getValue());
				} else if (configEntry.get() instanceof ConfigProperty<?>) {
					ConfigProperty<?> property = (ConfigProperty<?>) configEntry.get();

					try {
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
