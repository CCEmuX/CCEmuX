package net.clgd.ccemux.plugins.builtin;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;
import net.clgd.ccemux.api.OperatingSystem;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.PluginManager;
import net.clgd.ccemux.rendering.javafx.JFXRendererFactory;

@AutoService(Plugin.class)
public class JFXPlugin extends Plugin {
	public static final ConfigProperty<Boolean> forceUtilityDecoration = new ConfigProperty<>("forceUtilityDecoration",
		Boolean.class, false).setName("Force utility decoration")
		.setDescription("Uses utility window decoration instead of regular decoration. "
			+ "May fix problems with certain window managers.");

	public static final ConfigProperty<Boolean> doubleFontScale = new ConfigProperty<>("doubleFontScale", Boolean.class,
		OperatingSystem.get().equals(OperatingSystem.MacOSX)).setName("Double font resolution").setDescription(
		"Scales fonts by a factor of two before rendering. " + "May fix blurriness on high DPI displays.");

	@Nonnull
	@Override
	public String getName() {
		return "JavaFX Renderer";
	}

	@Nonnull
	@Override
	public String getDescription() {
		return "A renderer using JavaFX";
	}

	@Nonnull
	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Collection<String> getAuthors() {
		return Collections.singleton("CLGD");
	}

	@Nonnull
	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void configSetup(@Nonnull Group group) {
		group.addProperty(forceUtilityDecoration);
		group.addProperty(doubleFontScale);
	}

	@Override
	public void setup(@Nonnull PluginManager manager) {
		try {
			Class.forName("javafx.application.Application", false, getClass().getClassLoader());
		} catch (ClassNotFoundException ignored) {
			// Skip if JFX is not available
			return;
		}

		manager.addRenderer("JFX", new JFXRendererFactory());
	}
}
