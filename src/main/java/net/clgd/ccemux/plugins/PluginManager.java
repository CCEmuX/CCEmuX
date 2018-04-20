package net.clgd.ccemux.plugins;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.emulation.EmulatedComputer.Builder;
import net.clgd.ccemux.api.emulation.Emulator;
import net.clgd.ccemux.api.emulation.filesystem.VirtualDirectory;
import net.clgd.ccemux.api.peripheral.PeripheralFactory;
import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.hooks.*;
import net.clgd.ccemux.api.rendering.Renderer;
import net.clgd.ccemux.api.rendering.RendererFactory;

@Slf4j
public class PluginManager implements Closing, CreatingComputer, CreatingROM, ComputerCreated, ComputerRemoved,
		InitializationCompleted, RendererCreated, Tick, net.clgd.ccemux.api.plugins.PluginManager {
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
	private final Map<String, RendererFactory<?>> renderers = new HashMap<>();
	private final Map<String, PeripheralFactory<?>> peripherals = new HashMap<>();

	public PluginManager(EmuConfig cfg) {
		this.cfg = cfg;
	}

	public void gatherCandidates(ClassLoader loader) {
		Group cfgPlugins = cfg.group("plugins").setName("Plugins")
				.setDescription("Config options for the various plugins");
		for (Plugin candidate : ServiceLoader.load(Plugin.class, loader)) {
			Group cfgCandidate = cfgPlugins.group(candidate.getClass().getName()).setName(candidate.getName())
					.setDescription(candidate.getDescription());

			candidates.add(new PluginCandidate(candidate,
					cfgCandidate.property("enabled", Boolean.class, true).setName("Enabled")));

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
				log.info("Skipping disabled plugin [{}] from {}", candidate.plugin, source);
			}
		}
	}

	public void setup() {
		for (Plugin p : enabled) {
			try {
				log.debug("Calling setup for plugin [{}]", p);
				p.setup(this);
			} catch (Throwable t) {
				log.error("Exception while calling setup for plugin [{}]", p, t);

				log.info("Disabling plugin [{}]: setup failed", p);
				enabled.remove(p);
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
	public void onTick(Emulator emu, double dt) {
		doHooks(Tick.class, h -> h.onTick(emu, dt));
	}

	@Override
	public void onRendererCreated(Emulator emu, Renderer renderer) {
		doHooks(RendererCreated.class, h -> h.onRendererCreated(emu, renderer));
	}

	@Override
	public void onInitializationCompleted() {
		doHooks(InitializationCompleted.class, h -> h.onInitializationCompleted());
	}

	@Override
	public void onComputerRemoved(Emulator emu, EmulatedComputer computer) {
		doHooks(ComputerRemoved.class, h -> h.onComputerRemoved(emu, computer));
	}

	@Override
	public void onComputerCreated(Emulator emu, EmulatedComputer computer) {
		doHooks(ComputerCreated.class, h -> h.onComputerCreated(emu, computer));
	}

	@Override
	public void onCreatingComputer(Emulator emu, Builder builder) {
		doHooks(CreatingComputer.class, h -> h.onCreatingComputer(emu, builder));
	}

	@Override
	public void onClosing(Emulator emu) {
		doHooks(Closing.class, h -> h.onClosing(emu));
	}

	@Override
	public void onCreatingROM(Emulator emu, VirtualDirectory.Builder romBuilder) {
		doHooks(CreatingROM.class, h -> h.onCreatingROM(emu, romBuilder));
	}

	@Override
	public EmuConfig config() {
		return cfg;
	}

	@Override
	public void addRenderer(String name, RendererFactory<?> factory) {
		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(factory, "factory cannot be null");

		if (renderers.containsKey(name)) {
			throw new IllegalStateException("Renderer with name '" + name + "'  already registered.");
		}

		renderers.put(name, factory);
	}

	@Override
	public void addPeripheral(String name, PeripheralFactory<?> factory) {
		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(factory, "factory cannot be null");

		if (peripherals.containsKey(name)) {
			throw new IllegalStateException("Renderer with name '" + name + "'  already registered.");
		}

		peripherals.put(name, factory);
	}

	public Map<String, RendererFactory<?>> getRenderers() {
		return Collections.unmodifiableMap(renderers);
	}

	public Map<String, PeripheralFactory<?>> getPeripherals() {
		return Collections.unmodifiableMap(peripherals);
	}
}
