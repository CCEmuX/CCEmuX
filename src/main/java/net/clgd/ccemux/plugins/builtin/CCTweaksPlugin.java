package net.clgd.ccemux.plugins.builtin;

import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.swing.JOptionPane;

import org.squiddev.cctweaks.lua.ConfigMetadata;
import org.squiddev.cctweaks.lua.TweaksLogger;
import org.squiddev.cctweaks.lua.launch.ClassLoaderHelpers;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;
import org.squiddev.cctweaks.lua.lib.ApiRegister;

import com.google.auto.service.AutoService;

import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.api.config.ConfigEntry;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.plugins.Plugin;

@Slf4j
@AutoService(Plugin.class)
public class CCTweaksPlugin extends Plugin {
	private Group options;

	@Override
	public String getName() {
		return "CCTweaks";
	}

	@Override
	public String getDescription() {
		return "Adds modifications to CC, providing new APIs and other features";
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Override
	public Collection<String> getAuthors() {
		return Collections.singleton("SquidDev");
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional.of("https://github.com/SquidDev-CC/CCTweaks-Lua");
	}

	@Override
	public void configSetup(Group group) {
		options = group;
		for (ConfigMetadata.Category category : ConfigMetadata.categories()) {
			configSetup(group, category);
		}
	}

	private void configSetup(Group parent, ConfigMetadata.Category category) {
		Group group = parent.group(category.name());
		if (category.description() != null) group.setDescription(category.description());

		for (ConfigMetadata.Category child : category.children()) {
			configSetup(group, child);
		}

		for (ConfigMetadata.Property property : category.properties()) {
			ConfigProperty<?> groupProp = group.property(property.name(), property.type(), property.defaultValue());
			if (property.description() != null) groupProp.setName(property.name());
			groupProp.addAndFireListener((a, b) -> property.set(b));
		}
	}

	@Override
	public void loaderSetup(EmuConfig cfg, ClassLoader loader) {
		if (loader instanceof RewritingLoader) {
			TweaksLogger.instance = org.squiddev.patcher.Logger.instance = new org.squiddev.patcher.Logger() {
				@Override
				public void doDebug(String message) {
					log.debug(message);
				}

				@Override
				public void doWarn(String message) {
					log.warn(message);
				}

				@Override
				public void doError(String message, Throwable t) {
					log.error(message, t);
				}
			};

			RewritingLoader rwLoader = (RewritingLoader) loader;
			Optional<ConfigEntry> entry = options.group("testing").child("dumpAsm");
			if (entry.isPresent() && entry.get() instanceof ConfigProperty) {
				((ConfigProperty<Boolean>) entry.get()).addAndFireListener((a, b) -> rwLoader.dump(b));
			}

			try {
				ClassLoaderHelpers.setupChain((ClassLoader & RewritingLoader) loader);
			} catch (Exception e) {
				log.warn("Failed to apply classloader tweaks", e);
			}
		} else {
			log.warn("Incompatible ClassLoader in use - CCTweaks functionality unavailable");

			if (!GraphicsEnvironment.isHeadless()) {
				JOptionPane.showMessageDialog(null,
						"Your configuration is incompatible with the CCTweaks plugin.\n"
								+ "Please consult the logs for more information.",
						"CCTweaks unavailable", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	@Override
	public void setup(EmuConfig cfg) {
		ApiRegister.init();
		ApiRegister.loadPlugins();
	}
}
