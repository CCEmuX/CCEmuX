package net.clgd.ccemux.plugins.builtin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.squiddev.cctweaks.lua.TweaksLogger;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;
import org.squiddev.cctweaks.lua.lib.ApiRegister;

import com.google.auto.service.AutoService;

import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.plugins.Plugin;
import net.clgd.ccemux.plugins.config.JSONConfigHandler;
import net.clgd.ccemux.plugins.config.PluginConfigHandler;

@Slf4j
@AutoService(Plugin.class)
public class CCTweaksPlugin extends Plugin {

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
	public Optional<String> getAuthor() {
		return Optional.of("SquidDev");
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional.of("https://github.com/SquidDev-CC/CCTweaks-Lua");
	}

	// TODO: Make a better implementation that uses configgen?
	@Override
	public Optional<PluginConfigHandler<?>> getConfigHandler() {
		return Optional.of(new JSONConfigHandler<Map<String, String>>(new HashMap<String, String>()) {
			@Override
			public void configLoaded(Map<String, String> config) {
				config.forEach((k, v) -> System.setProperty("cctweaks." + k, v));
			}
		});
	}

	@Override
	public void loaderSetup(ClassLoader loader) {
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

		} else {
			log.warn("Incompatible ClassLoader in use - CCTweaks functionality unavailable");
		}
	}

	@Override
	public void setup() {
		ApiRegister.init();
		ApiRegister.loadPlugins();
	}

}
