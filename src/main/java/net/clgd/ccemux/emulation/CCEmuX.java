package net.clgd.ccemux.emulation;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import dan200.computercraft.core.filesystem.FileMount;
import net.clgd.ccemux.Config;

public class CCEmuX implements Runnable {

	private static final Field computerRootField;

	static {
		Field f;

		try {
			f = EmulatedComputer.class.getSuperclass().getDeclaredField("m_rootMount");
			f.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			f = null;
		}

		computerRootField = f;
	}

	public static String getVersion() {
		String v = Config.class.getPackage().getImplementationVersion();
		return v == null ? "[Unknown]" : v ;
	}

	public final Logger logger;
	public final Config conf;
	public final Path dataDir;
	public final File ccJar;

	public final EmulatedEnvironment env;

	private boolean running;
	private long timeStarted;
	private final List<EmulatedComputer> computers = new ArrayList<>();

	public CCEmuX(Logger logger, Config conf) {
		this.logger = logger;
		this.conf = conf;
		this.dataDir = conf.getDataDir();
		this.ccJar = dataDir.resolve(conf.getCCLocal()).toFile();

		env = new EmulatedEnvironment(this);
	}

	public EmulatedComputer createEmulatedComputer(int id, Path saveDir) {
		logger.trace("Creating emulated computer");
		synchronized (computers) {
			EmulatedComputer ec = new EmulatedComputer(this, conf.getTermWidth(), conf.getTermHeight(), id);
			logger.info("Created emulated computer ID {}", ec.getID());

			if (saveDir != null) {
				logger.info("Overriding save dir for computer {} to '{}'", ec.getID(), saveDir.toString());
				try {
					computerRootField.set(ec, new FileMount(saveDir.toFile(), env.getComputerSpaceLimit()));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			ec.turnOn();
			computers.add(ec);
			return ec;
		}
	}

	public EmulatedComputer createEmulatedComputer() {
		return createEmulatedComputer(-1, null);
	}

	public boolean removeEmulatedComputer(EmulatedComputer ec) {
		synchronized (computers) {
			logger.trace("Removing emulated computer ID {}", ec.getID());

			boolean success = computers.remove(ec);

			if (computers.isEmpty()) {
				running = false;
				logger.info("All emulated computers removed, stopping event loop");
			}

			return success;
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
	}

	@Override
	public void run() {
		running = true;

		timeStarted = System.currentTimeMillis();
		long lastTime = System.currentTimeMillis();

		while (running) {
			long now = System.currentTimeMillis();
			double dt = (now - lastTime) / 1000d;

			advance(dt);

			lastTime = System.currentTimeMillis();

			try {
				Thread.sleep(1000 / conf.getFramerate());
			} catch (InterruptedException ignored) {}
		}

		logger.debug("Emulation stopped");
	}

	public boolean isRunning() {
		return running;
	}

	public long getTimeStarted() {
		return timeStarted;
	}

	public double getTimeStartedInSeconds() {
		return timeStarted / 1000d;
	}

	public long getTimeSinceStart() {
		return System.currentTimeMillis() - timeStarted;
	}

	public double getSecondsSinceStart() {
		return getTimeSinceStart() / 1000d;
	}

	public long getTicksSinceStart() {
		return (int) (getSecondsSinceStart() * 20);
	}

	public static boolean getGlobalCursorBlink() {
		return System.currentTimeMillis() / 400 % 2 == 0;
	}

	public List<EmulatedComputer> getComputers() {
		return computers;
	}

}
