package net.clgd.ccemux.plugins.config;

import java.util.Optional;

/**
 * Plugin config handlers allow plugins to change the way that their config
 * files are read, written, and stored.
 * 
 * @author apemanzilla
 *
 * @param <T>
 */
public interface PluginConfigHandler<T> {
	/**
	 * Gets the file extension to be used when reading and writing the config
	 * file. The rest of the file name and path are determined using the plugin
	 * class name and cannot be overridden.
	 */
	public String getConfigFileExtension();

	/**
	 * Gets the default config to use if no config file exists or if it is
	 * empty.
	 */
	public T getDefaultConfig();

	/**
	 * Loads a config object from a <code>String</code>.
	 */
	public T loadConfig(String data);

	/**
	 * Converts a config to a <code>String</code> form which can be written to
	 * files.
	 */
	public String writeConfig(T config);

	/**
	 * Called after the plugin using this handler has been loaded, and after the
	 * config has been loaded as specified by this handler (either via a file or
	 * the given defaults). Unlike the other methods in this interface, this is
	 * intended to be used as a callback.
	 */
	public void configLoaded(T config);

	/**
	 * Calls {@link #configLoaded(Object)} using either the string loaded via
	 * {@link #loadConfig(String)}, or with the default config if the string is
	 * not present.
	 */
	public default void doLoadConfig(Optional<String> data) {
		configLoaded(data.map(this::loadConfig).orElse(getDefaultConfig()));
	}
}
