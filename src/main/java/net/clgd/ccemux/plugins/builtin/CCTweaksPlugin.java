package net.clgd.ccemux.plugins.builtin;

import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.swing.JOptionPane;

import org.squiddev.cctweaks.lua.TweaksLogger;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;
import org.squiddev.cctweaks.lua.lib.ApiRegister;

import com.google.auto.service.AutoService;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.clgd.ccemux.config.Group;
import net.clgd.ccemux.config.Property;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.plugins.Plugin;

@Slf4j
@AutoService(Plugin.class)
public class CCTweaksPlugin extends Plugin {
	private Property<Map<String, JsonPrimitive>> options;

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
		options = group.property("options", new TypeToken<Map<String, JsonPrimitive>>() {}, Collections.emptyMap())
				.setName("Options")
				.setDescription("Key-value configuration options to be passed to CCTweaks");
	}

	private void syncConfig() {
		for (val option : options.get().entrySet()) {
			System.setProperty("cctweaks." + option.getKey(), option.getValue().getAsString());
		}
	}

	@Override
	public void loaderSetup(EmuConfig cfg, ClassLoader loader) {
		syncConfig();

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

			try {
				// Thread.currentThread().setContextClassLoader(loader);

				rwLoader.loadConfig();
				rwLoader.loadChain();
			} catch (Exception e) {
				log.warn("Failed to apply classloader tweaks", e);
			}

			options.addListener((x, y) -> {
				syncConfig();
				try {
					rwLoader.loadConfig();
				} catch (Exception e) {
					log.warn("Failed to refresh config", e);
				}
			});
		} else {
			log.warn("Incompatible ClassLoader in use - CCTweaks functionality unavailable");

			if (!GraphicsEnvironment.isHeadless()) JOptionPane.showMessageDialog(null,
					"Your configuration is incompatible with the CCTweaks plugin.\n"
							+ "Please consult the logs for more information.",
					"CCTweaks unavailable", JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void setup(EmuConfig cfg) {
		ApiRegister.init();
		ApiRegister.loadPlugins();
	}

}
