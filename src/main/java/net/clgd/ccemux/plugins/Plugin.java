package net.clgd.ccemux.plugins;

import java.net.URL;
import java.util.Optional;

/**
 * Represents a plugin that can be loaded by CCEmuX.
 * 
 * @author apemanzilla
 *
 */
public abstract class Plugin {
	/**
	 * The name of the plugin. Should be short and concise - e.g. My Plugin.
	 */
	public abstract String getName();

	/**
	 * A brief description of the plugin and what it does.
	 */
	public abstract String getDescription();

	/**
	 * The version of the plugin. Format does not matter, but semantic
	 * versioning is recommended - e.g. <code>"1.2.3-alpha"</code>
	 */
	public abstract Optional<String> getVersion();

	/**
	 * The author of the plugin. If an empty <code>Optional</code> is returned,
	 * no author will be shown to end-users.
	 */
	public abstract Optional<String> getAuthor();

	/**
	 * Gets the website for this plugin. This can be a link to a forum thread, a
	 * wiki, source code, or anything else that may be helpful to end-users. If
	 * an empty <code>Optional</code> is returned, no website will be shown to
	 * end-users.
	 */
	public abstract Optional<URL> getWebsite();

	public void loaderSetup() {

	}

	/**
	 * Called when CCEmuX is starting, before any emulation occurs, used to load
	 * config files, register hooks, etc.
	 */
	public abstract void setup();
	
	public final String toString() {
		if (getVersion().isPresent())
			return getName() + " v" + getVersion().get();
		else
			return getName();
	}
}
