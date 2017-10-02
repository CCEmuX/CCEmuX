package net.clgd.ccemux.plugins;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.emulation.filesystem.VirtualDirectory;
import net.clgd.ccemux.init.Config;
import net.clgd.ccemux.plugins.config.PluginConfigHandler;
import net.clgd.ccemux.plugins.hooks.Closing;
import net.clgd.ccemux.plugins.hooks.ComputerCreated;
import net.clgd.ccemux.plugins.hooks.ComputerRemoved;
import net.clgd.ccemux.plugins.hooks.CreatingComputer;
import net.clgd.ccemux.plugins.hooks.CreatingROM;
import net.clgd.ccemux.plugins.hooks.Hook;
import net.clgd.ccemux.plugins.hooks.InitializationCompleted;
import net.clgd.ccemux.plugins.hooks.RendererCreated;
import net.clgd.ccemux.plugins.hooks.Tick;
import net.clgd.ccemux.rendering.Renderer;

@Slf4j
@SuppressWarnings("serial")
public class PluginManager extends HashSet<Plugin> implements Closing, CreatingComputer, CreatingROM, ComputerCreated,
		ComputerRemoved, InitializationCompleted, RendererCreated, Tick {
	private final Config cfg;

	public PluginManager(ClassLoader loader, Config cfg) {
		this.cfg = cfg;
		ServiceLoader.load(Plugin.class, loader).forEach(p -> {
			val source = p.getSource().map(File::getAbsolutePath).orElse("(unknown)");
			if (!cfg.isPluginBlacklisted(p)) {
				add(p);
				log.info("Loaded plugin [{}] from {}", p, source);
			} else {
				log.info("Skipping blacklisted plugin [{}] from {}", p, source);
			}
		});
	}

	public void loadConfigs() {
		Path configDir = cfg.getDataDir().resolve("plugins").resolve("configs");

		File cd = configDir.toFile();
		if (cd.isFile()) cd.delete();

		if (!cd.exists()) cd.mkdirs();

		stream().filter(p -> p.getConfigHandler().isPresent()).forEach(p -> {
			try {
				log.info("Loading config for plugin [{}]", p);

				PluginConfigHandler<?> h = p.getConfigHandler().get();

				File f = configDir.resolve(p.getClass().getName() + "." + h.getConfigFileExtension()).toFile();

				if (f.isFile() && f.length() > 0) {
					log.debug("Found user config file");
					h.doLoadConfig(Optional.of(FileUtils.readFileToString(f, (Charset) null)));
				} else {
					log.debug("Using default config");
					h.doLoadConfig(Optional.empty());
				}
			} catch (Exception e) {
				log.warn("Exception while loading config for plugin [{}]", p, e);
			}
		});
	}

	public void loaderSetup(ClassLoader loader) {
		forEach(p -> {
			try {
				log.debug("Calling loaderSetup for plugin [{}]", p);
				p.loaderSetup(loader);
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
