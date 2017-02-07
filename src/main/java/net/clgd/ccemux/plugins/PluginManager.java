package net.clgd.ccemux.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class PluginManager extends HashSet<Plugin> {
	private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

	public PluginManager( URL[] sources, ClassLoader parent) {
		URLClassLoader pluginLoader = new URLClassLoader(sources, parent);
		ServiceLoader.load(Plugin.class, pluginLoader).forEach(p -> {
			add(p);
			log.info("Loaded plugin [{}]", p);
		});
	}

	public void loaderSetup() {
		forEach(p -> {
			try {
				log.debug("Calling loaderSetup for plugin [{}]", p);
				p.loaderSetup();
			} catch (Exception e) {
				log.warn("Exception while calling loaderSetup for plugin [{}]: {}", p, e);
			}
		});
	}

	public void setup() {
		forEach(p -> {
			try {
				log.debug("Calling setup for plugin [{}]", p);
				p.setup();
			} catch (Exception e) {
				log.warn("Exception while calling setup for plugin [{}]: {}", p, e);
			}
		});
	}
}
