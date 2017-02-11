package net.clgd.ccemux.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.emulation.EmulatedComputer.Builder;
import net.clgd.ccemux.init.Config;
import net.clgd.ccemux.plugins.hooks.Closing;
import net.clgd.ccemux.plugins.hooks.CreatingComputer;
import net.clgd.ccemux.plugins.hooks.ComputerCreated;
import net.clgd.ccemux.plugins.hooks.ComputerRemoved;
import net.clgd.ccemux.plugins.hooks.InitializationCompleted;
import net.clgd.ccemux.plugins.hooks.RendererCreated;
import net.clgd.ccemux.plugins.hooks.Tick;
import net.clgd.ccemux.rendering.Renderer;

@SuppressWarnings("serial")
public class PluginManager extends HashSet<Plugin> implements Closing, CreatingComputer, ComputerCreated,
		ComputerRemoved, InitializationCompleted, RendererCreated, Tick {
	private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

	public PluginManager(URL[] sources, ClassLoader parent, Config cfg) {
		URLClassLoader pluginLoader = new URLClassLoader(sources, parent);
		ServiceLoader.load(Plugin.class, pluginLoader).forEach(p -> {
			if (!cfg.isPluginBlacklisted(p)) {
				add(p);
				log.info("Loaded plugin [{}]", p);
			} else {
				log.info("Skipping blacklisted plugin [{}]", p);
			}
		});
	}

	public void loaderSetup() {
		forEach(p -> {
			try {
				log.debug("Calling loaderSetup for plugin [{}]", p);
				p.loaderSetup();
			} catch (Exception e) {
				log.warn("Exception while calling loaderSetup for plugin [{}]", p, e);
			}
		});
	}

	public void setup() {
		forEach(p -> {
			try {
				log.debug("Calling setup for plugin [{}]", p);
				p.setup();
			} catch (Exception e) {
				log.warn("Exception while calling setup for plugin [{}]", p, e);
			}
		});
	}

	private <T extends Hook> void doHooks(Class<T> cls, Consumer<T> f) {
		forEach(p -> p.getHooks(cls).forEach(h -> {
			try {
				f.accept(h);
			} catch (Exception e) {
				log.warn("Exception while calling hook [{}] for plugin [{}]", cls.getName(), p, e);
			}
		}));
	}

	@Override
	public void onTick(CCEmuX emu, double dt) {
		doHooks(Tick.class, h -> h.onTick(emu, dt));
	}

	@Override
	public void onRendererCreated(CCEmuX emu, Renderer renderer) {
		doHooks(RendererCreated.class, h -> h.onRendererCreated(emu, renderer));
	}

	@Override
	public void onInitializationCompleted() {
		doHooks(InitializationCompleted.class, h -> h.onInitializationCompleted());
	}

	@Override
	public void onComputerRemoved(CCEmuX emu, EmulatedComputer computer) {
		doHooks(ComputerRemoved.class, h -> h.onComputerRemoved(emu, computer));
	}

	@Override
	public void onComputerCreated(CCEmuX emu, EmulatedComputer computer) {
		doHooks(ComputerCreated.class, h -> h.onComputerCreated(emu, computer));
	}

	@Override
	public void onCreatingComputer(CCEmuX emu, Builder builder) {
		doHooks(CreatingComputer.class, h -> h.onCreatingComputer(emu, builder));
	}

	@Override
	public void onClosing(CCEmuX emu) {
		doHooks(Closing.class, h -> h.onClosing(emu));
	}
}
