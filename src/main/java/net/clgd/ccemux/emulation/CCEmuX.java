package net.clgd.ccemux.emulation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.filesystem.ComboMount;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.JarMount;
import net.clgd.ccemux.init.CCEmuXConfig;
import net.clgd.ccemux.plugins.PluginManager;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.RendererConfig;
import net.clgd.ccemux.rendering.RendererFactory;

public class CCEmuX implements Runnable, IComputerEnvironment {
	private static final Logger log = LoggerFactory.getLogger(CCEmuX.class);

	public static String getVersion() {
		Package p = CCEmuX.class.getPackage();
		if (p != null) {
			return p.getImplementationVersion();
		} else {
			return null;
		}
	}

	public static boolean getGlobalCursorBlink() {
		return System.currentTimeMillis() / 400 % 2 == 0;
	}

	public final CCEmuXConfig cfg;
	private final PluginManager pluginMgr;
	public final File ccJar;

	private List<EmulatedComputer> computers = new ArrayList<>();

	private int nextID = 0;

	private long started = -1;
	private boolean running;

	public CCEmuX(CCEmuXConfig cfg, PluginManager pluginMgr, File ccJar) {
		this.cfg = cfg;
		this.pluginMgr = pluginMgr;
		this.ccJar = ccJar;
	}

	public EmulatedComputer addComputer(int id) {
		synchronized (computers) {
			EmulatedTerminal term = new EmulatedTerminal(cfg.getTermWidth(), cfg.getTermHeight());
			EmulatedComputer.Builder builder = EmulatedComputer.builder(this, term).id(id);

			pluginMgr.onComputerBeingCreated(this, builder);

			EmulatedComputer computer = builder.build();
			if (cfg.isApiEnabled()) computer.addAPI(new CCEmuXAPI(this, computer, "ccemux"));
			computers.add(computer);

			pluginMgr.onComputerCreated(this, computer);

			Renderer renderer = RendererFactory.implementations.get(cfg.getRenderer()).create(computer,
					new RendererConfig(cfg));

			computer.addListener(renderer);

			renderer.addListener(new Renderer.Listener() {
				@Override
				public void onClosed() {
					CCEmuX.this.removeComputer(computer);
				}
			});

			pluginMgr.onRendererCreated(this, renderer);

			renderer.setVisible(true);

			log.info("Created new computer ID {}", computer.getID());
			computer.turnOn();
			return computer;
		}
	}
	
	public EmulatedComputer addComputer() {
		return addComputer(-1);
	}

	public boolean removeComputer(EmulatedComputer computer) {
		synchronized (computers) {
			try {
				log.info("Removing computer ID {}", computer.getID());

				if (computers.remove(computer)) {
					pluginMgr.onComputerRemoved(this, computer);
					return true;
				} else {
					return false;
				}
			} finally {
				if (computers.size() < 1 && running) {
					log.info("All computers removed, stopping emulation");
					running = false;
				}
			}
		}
	}

	private void advance(double dt) {
		synchronized (computers) {
			computers.forEach(c -> {
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
			} catch (InterruptedException e) {}
		}

		log.info("Emulation stopped");
		started = -1;
	}

	public boolean isRunning() {
		return running;
	}

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
		if (path.startsWith("\\")) path = path.substring(1);

		try {
			return new ComboMount(new IMount[] { new JarMount(ccJar, path),
					new CustomRomMount(new ZipInputStream(this.getClass().getResourceAsStream("/custom.rom"))) });
		} catch (IOException e) {
			log.error("Failed to create resource mount", e);
			return null;
		}
	}

	@Override
	public IWritableMount createSaveDirMount(String path, long capacity) {
		return new FileMount(cfg.getDataDir().resolve(path).toFile(), cfg.getMaxComputerCapaccity());
	}

	@Override
	public long getComputerSpaceLimit() {
		return cfg.getMaxComputerCapaccity();
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
