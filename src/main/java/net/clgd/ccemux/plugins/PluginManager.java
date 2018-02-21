package net.clgd.ccemux.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.clgd.ccemux.config.Group;
import net.clgd.ccemux.config.ConfigProperty;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.emulation.filesystem.VirtualDirectory;
import net.clgd.ccemux.plugins.hooks.*;
import net.clgd.ccemux.rendering.Renderer;

@Slf4j
public class PluginManager implements Closing, CreatingComputer, CreatingROM, ComputerCreated,
		ComputerRemoved, InitializationCompleted, RendererCreated, Tick {
	private static class PluginCandidate {
		final Plugin plugin;
		final ConfigProperty<Boolean> enabled;

		private PluginCandidate(Plugin plugin, ConfigProperty<Boolean> enabled) {
			this.plugin = plugin;
			this.enabled = enabled;
		}
	}

	private final EmuConfig cfg;

	private final List<PluginCandidate> candidates = new ArrayList<>();
	private final List<Plugin> enabled = new ArrayList<>();

	public PluginManager(EmuConfig cfg) {
		this.cfg = cfg;
	}

	public void gatherCandidates(ClassLoader loader) {
		Group cfgPlugins = cfg.group("plugins")
				.setName("Plugins")
				.setDescription("Config options for the various plugins");
		for (Plugin candidate : ServiceLoader.load(Plugin.class, loader)) {
			Group cfgCandidate = cfgPlugins.group(candidate.getClass().getName())
					.setName(candidate.getName())
					.setDescription(candidate.getDescription());

			candidates.add(new PluginCandidate(
					candidate,
					cfgCandidate.property("enabled", Boolean.class, true).setName("Enabled")
			));

			candidate.configSetup(cfgCandidate);
		}
	}

	public void gatherEnabled() {
		for (PluginCandidate candidate : candidates) {
			val source = candidate.plugin.getSource().map(File::getAbsolutePath).orElse("(unknown)");
			if (candidate.enabled.get()) {
				enabled.add(candidate.plugin);
				log.info("Loaded plugin [{}] from {}", candidate.plugin, source);
			} else {
				log.info("Skipping blacklisted plugin [{}] from {}", candidate.plugin, source);
			}
		}
	}

	public void loaderSetup(ClassLoader loader) {
		for (Plugin p : enabled) {
			try {
				log.debug("Calling loaderSetup for plugin [{}]", p);
				p.loaderSetup(cfg, loader);
			} catch (Exception e) {
				log.warn("Exception while calling loaderSetup for plugin [{}]", p, e);
			}
		}
	}

	public void setup() {
		for (Plugin p : enabled) {
			try {
				log.debug("Calling setup for plugin [{}]", p);
				p.setup(cfg);
			} catch (Exception e) {
				log.warn("Exception while calling setup for plugin [{}]", p, e);
			}
		}
	}

	private <T extends Hook> void doHooks(Class<T> cls, Consumer<T> f) {
		for (Plugin p : enabled) {
			for (T h : p.getHooks(cls)) {
				try {
					f.accept(h);
				} catch (Exception e) {
					log.warn("Exception while calling hook [{}] for plugin [{}]", cls.getName(), p, e);
				}
			}
		}
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
