package net.clgd.ccemux.emulation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.computer.IComputerEnvironment;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.emulation.EmulatedTerminal;

/**
 * Represents a computer that can be emulated via CCEmuX
 */
@Slf4j
public class EmulatedComputerImpl extends EmulatedComputer {
	private static final Field rootMountField;
	private static final Field stateField;
	private static final Field startRequestedField;

	static {
		rootMountField = getField("m_rootMount", "root mount");
		stateField = getField("m_state", "state");
		startRequestedField = getField("m_startRequested", "start requested");
	}

	private static Field getField(String name, String description) {
		try {
			Field field = EmulatedComputer.class.getSuperclass().getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException e) {
			log.error("Failed to get computer " + description + " field", e);
			return null;
		}
	}

	/**
	 * A class used to create new {@link EmulatedComputer} instances
	 *
	 * @author apemanzilla
	 */
	public static class BuilderImpl implements Builder {
		private final IComputerEnvironment env;
		private final EmulatedTerminal term;

		private Integer id = null;

		private IWritableMount rootMount = null;

		private String label = null;

		private transient boolean built = false;

		private BuilderImpl(IComputerEnvironment env, EmulatedTerminal term) {
			this.env = env;
			this.term = term;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.clgd.ccemux.emulation.Builder#id(java.lang.Integer)
		 */
		@Nonnull
		@Override
		public BuilderImpl id(Integer num) {
			id = num;
			return this;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.clgd.ccemux.emulation.Builder#rootMount(dan200.computercraft.api.
		 * filesystem.IWritableMount)
		 */
		@Nonnull
		@Override
		public Builder rootMount(IWritableMount rootMount) {
			this.rootMount = rootMount;
			return this;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.clgd.ccemux.emulation.Builder#label(java.lang.String)
		 */
		@Nonnull
		@Override
		public Builder label(String label) {
			this.label = label;
			return this;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.clgd.ccemux.emulation.Builder#build()
		 */
		@Nonnull
		@Override
		public EmulatedComputer build() {
			if (!built) {
				EmulatedComputer ec = new EmulatedComputerImpl(env, term, Optional.ofNullable(id).orElse(-1));
				ec.assignID();

				if (label != null) {
					ec.setLabel(label);
				}

				try {
					if (rootMount != null) {
						rootMountField.set(ec, rootMount);
					} else {
						rootMountField.set(ec, env.createSaveDirMount(Integer.toString(ec.getID()), 2 * 1024 * 1024));
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException("Failed to set root mount while building computer ID " + ec.getID(), e);
				}

				return ec;
			} else {
				throw new IllegalStateException("This computer has already been built!");
			}
		}
	}

	/**
	 * Gets a new builder to create an {@link EmulatedComputer} instance.
	 *
	 * @return The new builder
	 */
	public static Builder builder(IComputerEnvironment env, EmulatedTerminal term) {
		return new BuilderImpl(env, term);
	}

	/**
	 * The terminal that this computer draws to
	 */
	private final EmulatedTerminal terminal;

	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

	private EmulatedComputerImpl(IComputerEnvironment environment, EmulatedTerminal terminal, int id) {
		super(environment, terminal, id);
		this.terminal = terminal;
	}

	@Override
	public boolean addListener(@Nonnull Listener l) {
		return listeners.add(l);
	}

	@Override
	public boolean removeListener(@Nonnull Listener l) {
		return listeners.remove(l);
	}

	@Override
	public void advance(double dt) {
		super.advance(dt);

		for (int i = 0; i < 6; i++) {
			IPeripheral peripheral = getPeripheral(i);
			if (peripheral instanceof Listener) ((Listener) peripheral).onAdvance(dt);
		}

		listeners.forEach(l -> l.onAdvance(dt));
	}

	@Override
	public void copyFiles(@Nonnull Iterable<File> files, @Nonnull String location) throws IOException {
		val mount = this.getRootMount();
		val base = Paths.get(location);

		for (val f : files) {
			val path = base.resolve(f.getName()).toString();

			if (f.isFile()) {
				if (f.length() > mount.getRemainingSpace()) {
					throw new IOException("Not enough space on computer");
				}

				val s = mount.openForWrite(path);
				IOUtils.copy(FileUtils.openInputStream(f), s);
			} else {
				mount.makeDirectory(path);
				copyFiles(Arrays.asList(f.listFiles()), path);
			}
		}
	}

	@Override
	public boolean isShutdown() {
		if (stateField != null && startRequestedField != null) {
			try {
				// Is the computer off and no start has been requested
				return stateField.get(this).toString().equals("Off") && !startRequestedField.getBoolean(this);
			} catch (IllegalAccessException ignored) {
			}
		}

		return !isOn();
	}
}
