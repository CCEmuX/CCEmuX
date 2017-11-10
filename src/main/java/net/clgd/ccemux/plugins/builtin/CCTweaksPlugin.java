package net.clgd.ccemux.plugins.builtin;

import java.awt.GraphicsEnvironment;
import java.util.Optional;
import java.util.Set;

import javax.swing.JOptionPane;

import com.google.common.collect.Sets;
import org.squiddev.cctweaks.lua.TweaksLogger;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;
import org.squiddev.cctweaks.lua.lib.ApiRegister;

import com.google.auto.service.AutoService;

import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.plugins.Plugin;

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
	public Set<String> getAuthors() {
		return Sets.newHashSet("SquidDev");
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional.of("https://github.com/SquidDev-CC/CCTweaks-Lua");
	}

	private void applyConfig(EmuConfig cfg) {
		cfg.keys().stream().filter(k -> k.startsWith("cctweaks."))
				.forEach(k -> System.setProperty(k, cfg.getAs(k, String.class).get()));
	}

	@Override
	public void loaderSetup(EmuConfig cfg, ClassLoader loader) {
		applyConfig(cfg);

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
