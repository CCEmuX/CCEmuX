package net.clgd.ccemux.plugins.builtin;

import java.net.URL;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;
import org.squiddev.cctweaks.lua.lib.ApiRegister;

import net.clgd.ccemux.plugins.Plugin;

public class CCTweaksPlugin extends Plugin {
	private static final Logger log = LoggerFactory.getLogger(CCTweaksPlugin.class);

	@Override
	public String getName() {
		return "CCTweaks";
	}

	@Override
	public String getDescription() {
		return "Adds CCTweaks functionality";
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
	public Optional<URL> getWebsite() {
		return Optional.empty();
	}
	
	@Override
	public void loaderSetup() {
		if (getClass().getClassLoader() instanceof RewritingLoader) {
			
			org.squiddev.patcher.Logger.instance = new org.squiddev.patcher.Logger() {
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
			
			RewritingLoader loader = (RewritingLoader) getClass().getClassLoader();
			
			try {
				//Thread.currentThread().setContextClassLoader(loader);
				
				loader.loadConfig();
				loader.loadChain();
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
