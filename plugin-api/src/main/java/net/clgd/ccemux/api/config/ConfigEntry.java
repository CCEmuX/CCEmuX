package net.clgd.ccemux.api.config;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * A keyed entry in a config file.
 *
 * @see Group
 * @see ConfigProperty
 */
public abstract class ConfigEntry {
	ConfigEntry() {}

	/**
	 * Get the unique key for this entry.
	 *
	 * @return The entry's key.
	 */
	@Nonnull
	public abstract String getKey();

	/**
	 * Get a friendly name for this entry.
	 *
	 * @return A friendly name, defaulting to {@link #getKey()} if none is set.
	 */
	@Nonnull
	public abstract String getName();

	/**
	 * Get a short description of what this entry does.
	 *
	 * @return A short description.
	 */
	@Nonnull
	public abstract Optional<String> getDescription();
}
