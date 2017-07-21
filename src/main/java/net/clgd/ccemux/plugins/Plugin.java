package net.clgd.ccemux.plugins;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.plugins.config.PluginConfigHandler;
import net.clgd.ccemux.plugins.hooks.Hook;

/**
 * Represents a plugin for CCEmuX. Plugins can add or change behavior, such as
 * adding Lua APIs, adding rendering systems, or even changing the behavior of
 * CC itself. (albeit through classloader hacks)
 * 
 * @author apemanzilla
 * @see Hook
 */
@Slf4j
public abstract class Plugin {
	private final Set<Hook> hooks = new HashSet<>();

	/**
	 * Gets all the hooks this plugin has registered
	 * 
	 * @see Hook
	 */
	public final Set<Hook> getHooks() {
		return Collections.unmodifiableSet(hooks);
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
		return Collections.unmodifiableSet(Collections.unmodifiableSet(hooks.stream()
				.filter(h -> cls.isAssignableFrom(h.getClass())).map(h -> (T) h).collect(Collectors.toSet())));
	}

	/**
	 * Registers a new hook which may be called later.
	 *
	 * @see Hook
	 * @see #registerHook(Class, Hook)
	 */
	protected final void registerHook(Hook hook) {
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
	public abstract Optional<String> getWebsite();

	/**
	 * Gets the config handler for this plugin.
	 * 
	 * @see net.clgd.ccemux.plugins.config.PluginConfigHandler
	 *      PluginConfigHandler
	 */
	public Optional<PluginConfigHandler<?>> getConfigHandler() {
		return Optional.empty();
	}

	/**
	 * Called early while CCEmuX is starting, before even CC itself is loaded.
	 * This method is intended to be used to interact with the classloader
	 * before CC is loaded and should not be used unless you know what you're
	 * doing!
	 * 
	 * @see #setup()
	 */
	public void loaderSetup(ClassLoader loader) {};

	/**
	 * Called while CCEmuX is starting. This method should be used to register
	 * hooks, or renderers.<br />
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
	
	/**
	 * Attempts to locate the file that this plugin was loaded from
	 * @return
	 */
	public final Optional<File> getSource() {
		try {
			return Optional.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
		} catch (URISyntaxException | NullPointerException | SecurityException e) {
			log.error("Failed to locate plugin source for plugin {}", toString(), e);
			return Optional.empty();
		}
	}
}
