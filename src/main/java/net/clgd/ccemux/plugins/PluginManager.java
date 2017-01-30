package net.clgd.ccemux.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.ServiceLoader;

import org.slf4j.Logger;

@SuppressWarnings("serial")
public class PluginManager extends HashSet<Plugin> {
	private final Logger logger;

	public PluginManager(Logger logger, URL[] sources, ClassLoader parent) {
		this.logger = logger;

		URLClassLoader pluginLoader = new URLClassLoader(sources, parent);
		ServiceLoader.load(Plugin.class, pluginLoader).forEach(p -> {
			add(p);
			logger.info("Loaded plugin [{}]", p.getName());
		});
	}

	public void loaderSetup() {
		forEach(p -> {
			try {
				logger.debug("Calling loaderSetup for plugin [{}]", p.getName());
				p.loaderSetup();
			} catch (Exception e) {
				logger.warn("Exception while calling loaderSetup for plugin [" + p.getName() + ']', e);
			}
		});
	}

	public void setup() {
		forEach(p -> {
			try {
				logger.debug("Calling setup for plugin [{}]", p.getName());
				p.setup();
			} catch (Exception e) {
				logger.warn("Exception while calling setup for plugin [" + p.getName() + ']', e);
			}
		});
	}
}
