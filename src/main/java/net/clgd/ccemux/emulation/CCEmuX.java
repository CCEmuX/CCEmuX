package net.clgd.ccemux.emulation;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.filesystem.ComboMount;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.JarMount;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.emulation.EmulatedTerminal;
import net.clgd.ccemux.api.emulation.Emulator;
import net.clgd.ccemux.api.emulation.filesystem.VirtualDirectory;
import net.clgd.ccemux.api.emulation.filesystem.VirtualMount;
import net.clgd.ccemux.api.peripheral.PeripheralFactory;
import net.clgd.ccemux.api.rendering.Renderer;
import net.clgd.ccemux.api.rendering.RendererFactory;
import net.clgd.ccemux.init.UserConfig;
import net.clgd.ccemux.plugins.PluginManager;

@RequiredArgsConstructor
public class CCEmuX implements Runnable, Emulator, IComputerEnvironment {
	private static final Logger log = LoggerFactory.getLogger(CCEmuX.class);

	public static String getVersion() {
		try (InputStream s = CCEmuX.class.getResourceAsStream("/ccemux.version")) {
			Properties props = new Properties();
			props.load(s);
			return props.getProperty("version");
		} catch (IOException e) {
			return "UNKNOWN";
		}
	}

	private final UserConfig cfg;

	@Getter
	private final RendererFactory<?> rendererFactory;

	@Getter
	private final PluginManager pluginMgr;

	private final File ccSource;

	private final Path sessionPath;

	private final Map<EmulatedComputer, Renderer> computers = new ConcurrentHashMap<>();

	private int nextID = 0;

	private long started = -1;
	private boolean running;

	@Nonnull
	@Override
	public EmuConfig getConfig() {
		return cfg;
	}

	@Nonnull
	@Override
	public File getCCJar() {
		return ccSource;
	}

	@Nonnull
	@Override
	public String getEmulatorVersion() {
		return getVersion();
	}

	@Override
	public PeripheralFactory<?> getPeripheralFactory(@Nonnull String name) {
		return pluginMgr.getPeripherals().get(name);
	}

	/**
	 * Creates a new computer and renderer, applying config settings and plugin
	 * hooks appropriately.
	 *
	 * @return The new computer
	 * @see #createComputer(Consumer)
	 */
	@Override
	@Nonnull
	public EmulatedComputer createComputer() {
		return createComputer(b -> {
		});
	}

	/**
	 * Creates a new computer and renderer, applying config settings and plugin
	 * hooks appropriately. Additionally takes a {@link Consumer} which will be
	 * called on the {@link EmulatedComputer.Builder} after plugin hooks, which
	 * can be used to change the computers ID or other properties.
	 *
	 * @param builderMutator Will be called after plugin hooks with the builder
	 * @return The new computer
	 */
	@Override
	@Nonnull
	public EmulatedComputer createComputer(@Nonnull Consumer<EmulatedComputer.Builder> builderMutator) {
		EmulatedTerminal term = new EmulatedTerminal(cfg.termWidth.get(), cfg.termHeight.get());
		EmulatedComputer.Builder builder = EmulatedComputerImpl.builder(this, term).id(-1);

		pluginMgr.onCreatingComputer(this, builder);
		builderMutator.accept(builder);

		EmulatedComputer computer = builder.build();

		pluginMgr.onComputerCreated(this, computer);

		addComputer(computer);

		return computer;
	}

	private void addComputer(EmulatedComputer ec) {
		Renderer r = rendererFactory.create(ec, cfg);

		ec.addListener(r);
		r.addListener(() -> this.removeComputer(ec)); // onClose

		pluginMgr.onRendererCreated(this, r);

		computers.put(ec, r);

		r.setVisible(true);

		log.info("Created new computer ID {}", ec.getID());
		ec.turnOn();

		sessionStateChanged();
	}

	@Override
	public boolean removeComputer(@Nonnull EmulatedComputer computer) {
		synchronized (computers) {
			try {
				log.info("Removing computer ID {}", computer.getID());

				Renderer renderer = computers.remove(computer);
				if (renderer != null) {
					renderer.dispose();
					pluginMgr.onComputerRemoved(this, computer);
					return true;
				} else {
					return false;
				}
			} finally {
				if (computers.isEmpty() && running) {
					log.info("All computers removed, stopping emulation");
					running = false;
				} else {
					sessionStateChanged();
				}
			}
		}
	}

	void sessionStateChanged() {
		if (cfg.restoreSession.get() && running) {
			new SessionState(
				computers.keySet().stream()
					.map(x -> new SessionState.ComputerState(x.getID(), x.getLabel()))
					.collect(Collectors.toList())
			).save(sessionPath);
		}
	}

	private void advance(double dt) {
		synchronized (computers) {
			computers.keySet().forEach(c -> {
				synchronized (c) {
					c.advance(dt);
				}
			});
		}

		pluginMgr.onTick(this, dt);
	}

	@Override
	public void run() {
		running = true;
		started = System.currentTimeMillis();

		// Save the state if we turn on session persistence
		BiConsumer<Boolean, Boolean> persistSessionListener = (from, to) -> sessionStateChanged();
		cfg.restoreSession.addListener(persistSessionListener);

		long lastTime = started;
		double computerTickTimer = 0d;

		while (running) {
			long now = System.currentTimeMillis();
			double dt = (now - lastTime) / 1000d;

			computerTickTimer += dt;

			if (computerTickTimer >= 0.05d) {
				advance(dt);
				computerTickTimer = 0d;
			}

			lastTime = now;

			try {
				Thread.sleep(Math.max(0, 50 - (System.currentTimeMillis() - now)));
			} catch (InterruptedException ignored) {
			}
		}

		log.info("Emulation stopped");

		// Clean up anything we no longer need
		cfg.restoreSession.removeListener(persistSessionListener);
		started = -1;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void stop() {
		running = false;
	}

	public long getTicksSinceStart() {
		return (System.currentTimeMillis() - started) / 50;
	}

	@Override
	public int assignNewID() {
		return nextID++;
	}

	@Override
	public IMount createResourceMount(String domain, String subPath) {
		String path = Paths.get("assets", domain, subPath).toString().replace('\\', '/');
		if (path.startsWith("/")) path = path.substring(1);

		JarMount jarMount;
		try {
			jarMount = new JarMount(ccSource, path);
		} catch (IOException e) {
			log.error("Could not create mount from mod jar", e);
			return null;
		}

		VirtualDirectory.Builder romBuilder = new VirtualDirectory.Builder();
		pluginMgr.onCreatingROM(this, romBuilder);

		return new ComboMount(new IMount[] {
			// From ComputerCraft JAR
			jarMount,
			// From plugin files
			new VirtualMount(romBuilder.build()),
			// From data directory
			new FileMount(cfg.getDataDir().resolve(path).toFile(), 0)
		});
	}

	@Override
	public InputStream createResourceFile(String domain, String subPath) {
		String path = Paths.get("assets", domain, subPath).toString().replace('\\', '/');
		if (path.startsWith("/")) path = path.substring(1);

		File assetFile = cfg.getDataDir().resolve(path).toFile();
		if (assetFile.exists() && assetFile.isFile()) {
			try {
				return new FileInputStream(assetFile);
			} catch (FileNotFoundException e) {
				log.error("Failed to create resource file", e);
			}
		}

		return CCEmuX.class.getClassLoader().getResourceAsStream(path);
	}

	@Override
	public IWritableMount createSaveDirMount(String path, long capacity) {
		return new FileMount(cfg.getComputerDir().resolve(path).toFile(), getComputerSpaceLimit());
	}

	@Override
	public long getComputerSpaceLimit() {
		return cfg.maxComputerCapacity.get();
	}

	@Override
	public int getDay() {
		return (int) (((getTicksSinceStart() + 6000) / 24000) + 1);
	}

	@Override
	public String getHostString() {
		if (getVersion() != null) {
			return String.format("ComputerCraft %s (CCEmuX %s)", ComputerCraft.getVersion(), getVersion());
		} else {
			return String.format("ComputerCraft %s (CCEmuX)", ComputerCraft.getVersion());
		}
	}

	@Override
	public double getTimeOfDay() {
		return ((getTicksSinceStart() + 6000) % 24000) / 1000d;
	}

	@Override
	public boolean isColour() {
		return true;
	}
}
