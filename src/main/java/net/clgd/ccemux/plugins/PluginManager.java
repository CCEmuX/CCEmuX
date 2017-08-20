package net.clgd.ccemux.plugins;

import java.io.File;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.emulation.*;
import net.clgd.ccemux.emulation.filesystem.VirtualDirectory;
import net.clgd.ccemux.plugins.hooks.*;
import net.clgd.ccemux.rendering.Renderer;

@Slf4j
@SuppressWarnings("serial")
public class PluginManager extends HashSet<Plugin> implements Closing, CreatingComputer, CreatingROM, ComputerCreated,
		ComputerRemoved, InitializationCompleted, RendererCreated, Tick {
	private final EmuConfig cfg;

	public PluginManager(ClassLoader loader, EmuConfig cfg) {
		this.cfg = cfg;
		ServiceLoader.load(Plugin.class, loader).forEach(p -> {
			val source = p.getSource().map(File::getAbsolutePath).orElse("(unknown)");
			if (cfg.pluginEnabled(p).get()) {
				add(p);
				log.info("Loaded plugin [{}] from {}", p, source);
			} else {
				log.info("Skipping blacklisted plugin [{}] from {}", p, source);
			}
		});
	}

	public void loaderSetup(ClassLoader loader) {
		forEach(p -> {
			try {
				log.debug("Calling loaderSetup for plugin [{}]", p);
				p.loaderSetup(cfg, loader);
			} catch (Exception e) {
				log.warn("Exception while calling loaderSetup for plugin [{}]", p, e);
			}
		});
	}

	public void setup() {
		forEach(p -> {
			try {
				log.debug("Calling setup for plugin [{}]", p);
				p.setup(cfg);
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
	public void onCreatingComputer(CCEmuX emu, EmulatedComputer.Builder builder) {
		doHooks(CreatingComputer.class, h -> h.onCreatingComputer(emu, builder));
	}

	@Override
	public void onClosing(CCEmuX emu) {
		doHooks(Closing.class, h -> h.onClosing(emu));
	}

	@Override
	public void onCreatingROM(CCEmuX emu, VirtualDirectory.Builder romBuilder) {
		doHooks(CreatingROM.class, h -> h.onCreatingROM(emu, romBuilder));
	}
}
