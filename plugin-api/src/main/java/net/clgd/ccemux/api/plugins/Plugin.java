package net.clgd.ccemux.api.plugins;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import dan200.computercraft.api.lua.ILuaAPI;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.plugins.hooks.Hook;
import net.clgd.ccemux.api.rendering.RendererFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entry interface for a CCEmuX. Plugins can add or change behavior, such as
 * {@linkplain EmulatedComputer#addApi(ILuaAPI)} adding Lua APIs} or
 * {@linkplain PluginManager#addRenderer(String, RendererFactory) registering rendering plugins}.
 * <p>
 * Plugins are loaded via Java's {@link ServiceLoader} mechanism. Implementing classes should be listed in
 * a {@code META-INF/services/net.clgd.ccemux.api.plugins.Plugin} file - it is recommended one uses
 * <a href="https://github.com/google/auto/tree/main/service">the {@code @AutoService} annotation</a> to generate this
 * file.
 * <p>
 * Plugins typically implement their behaviour by registering various hooks with the {@link #registerHook(Hook)} method.
 * See subclasses of the {@link Hook} interface for useful behaviour.
 *
 * @author apemanzilla
 * @see Hook
 */
public abstract class Plugin {
	private static final Logger log = LoggerFactory.getLogger(Plugin.class);
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
	 * @param cls The type
	 * @return A set of all hooks of the given type
	 */
	@Nonnull
	public final <T extends Hook> Set<T> getHooks(@Nonnull Class<T> cls) {
		return hooks.stream().filter(h -> cls.isAssignableFrom(h.getClass())).map(cls::cast).collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * Registers a new hook which may be called later.
	 *
	 * @see Hook
	 * @see #registerHook(Class, Hook)
	 */
	protected final void registerHook(@Nonnull Hook hook) {
		hooks.add(hook);
	}

	/**
	 * Registers a new hook which may be called later. The {@code cls}
	 * parameter is only used to help out with type inference so that lambdas
	 * can be used.
	 *
	 * @deprecated Using this method with lambdas as opposed to
	 * {@link #registerHook(Hook)} with anonymous classes may cause
	 * crashes, as lambdas force the JVM to load classes earlier
	 * than usual, which can result in a
	 * {@link ClassNotFoundException} because of the way
	 * ComputerCraft is loaded at runtime. This method will most
	 * likely be removed in the future.
	 */
	@Deprecated
	protected final <T extends Hook> void registerHook(@Nonnull Class<T> cls, @Nonnull T hook) {
		registerHook(hook);
	}

	/**
	 * The name of the plugin. Should be short and concise - e.g. My Plugin.
	 */
	@Nonnull
	public abstract String getName();

	/**
	 * A brief description of the plugin and what it does.
	 */
	@Nonnull
	public abstract String getDescription();

	/**
	 * The version of the plugin. Format does not matter, but semantic
	 * versioning is recommended - e.g. {@code "1.2.3-alpha"}.
	 */
	@Nonnull
	public abstract Optional<String> getVersion();

	/**
	 * The authors of the plugin. If an empty {@link Collection} is returned,
	 * no authors will be shown to end-users.
	 */
	@Nonnull
	public abstract Collection<String> getAuthors();

	/**
	 * Gets the website for this plugin. This can be a link to a forum thread, a
	 * wiki, source code, or anything else that may be helpful to end-users. If
	 * an empty {@link Optional} is returned, no website will be shown to
	 * end-users.
	 */
	@Nonnull
	public abstract Optional<String> getWebsite();

	/**
	 * Setup any configuration options this plugin requires.
	 *
	 * This is called before anything else is loaded, and so one should be careful
	 * not to reference any CC classes.
	 *
	 * @param group The group to load config elements from.
	 */
	public void configSetup(@Nonnull Group group) {}

	/**
	 * Called while CCEmuX is starting. This method should be used to register
	 * hooks, or renderers.
	 *
	 * In order to prevent issues, any setup code that needs to interact with CC
	 * should use the
	 * {@link net.clgd.ccemux.api.plugins.hooks.InitializationCompleted
	 * InitializationCompleted} hook.
	 *
	 * @see Hook
	 */
	public abstract void setup(@Nonnull PluginManager manager);

	@Nonnull
	public final String toString() {
		return getName() + getVersion().map(v -> " v" + v).orElse("");
	}

	/**
	 * Attempts to locate the file that this plugin was loaded from
	 *
	 * @return The file that this plugin is declared in
	 */
	@Nonnull
	public final Optional<File> getSource() {
		try {
			return Optional.of(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
		} catch (URISyntaxException | NullPointerException | SecurityException e) {
			log.error("Failed to locate plugin source for plugin {}", toString(), e);
			return Optional.empty();
		}
	}
}
