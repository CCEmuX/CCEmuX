package net.clgd.ccemux.plugins.builtin;

import java.util.*;

import com.google.auto.service.AutoService;

import net.clgd.ccemux.api.OperatingSystem;
import net.clgd.ccemux.config.ConfigProperty;
import net.clgd.ccemux.config.Group;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.plugins.Plugin;
import net.clgd.ccemux.rendering.RendererFactory;
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

	@Override
	public String getName() {
		return "JavaFX Renderer";
	}

	@Override
	public String getDescription() {
		return "A renderer using JavaFX";
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Override
	public Collection<String> getAuthors() {
		return Collections.singleton("CLGD");
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void configSetup(Group group) {
		group.addProperty(forceUtilityDecoration);
		group.addProperty(doubleFontScale);
	}

	@Override
	public void setup(EmuConfig cfg) {
		RendererFactory.implementations.put("JFX", new JFXRendererFactory());
	}
}
