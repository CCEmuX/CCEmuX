package net.clgd.ccemux.plugins;

import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a plugin for CCEmuX. Plugins can add or change behavior, such as
 * adding Lua APIs, adding rendering systems, or even changing the behavior of
 * CC itself. (albeit through classloader hacks)
 * 
 * @author apemanzilla
 * @see Hook
 */
public abstract class Plugin {
	private final Set<Hook> hooks = new HashSet<>();

	/**
	 * Gets all the hooks this plugin has registered
	 * 
	 * @see Hook
	 */
	public final Set<Hook> getHooks() {
		return hooks;
	}

	/**
	 * Gets all the hooks this plugin has registered of a specific type
	 * 
	 * @param cls
	 *            The type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Hook> Set<T> getHooks(Class<T> cls) {
		return hooks.stream().filter(h -> cls.isAssignableFrom(h.getClass())).map(h -> (T) h)
				.collect(Collectors.toSet());
	}

	/**
	 * Registers a new hook which may be called later.
	 *
	 * @see Hook
	 * @see #registerHook(Class, Hook)
	 */
	public final void registerHook(Hook hook) {
		hooks.add(hook);
	}

	/**
	 * Registers a new hook which may be called later. The <code>cls</code>
	 * parameter is only used to help out with type inference so that lambdas
	 * can be used.
	 * 
	 * @see Hook
	 * @see #registerHook(Hook)
	 */
	public final <T extends Hook> void registerHook(Class<T> cls, T hook) {
		hooks.add(hook);
	}

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
	 * 
	 */
	public abstract Optional<URL> getWebsite();

	public void loaderSetup() {};

	/**
	 * Called early while CCEmuX is starting, before even CC itself is loaded.
	 * This method should be used to register hooks, or renderers.<br />
	 * <br />
	 * In order to prevent issues, any setup code that needs to interact with CC
	 * should use the
	 * {@link net.clgd.ccemux.plugins.hooks.InitializationCompleted
	 * InitializationCompleted} hook.
	 * 
	 * @see Hook
	 */
	public abstract void setup();

	public final String toString() {
		if (getVersion().isPresent())
			return getName() + " v" + getVersion().get();
		else
			return getName();
	}
}
