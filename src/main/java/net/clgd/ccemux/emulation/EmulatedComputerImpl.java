package net.clgd.ccemux.emulation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

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
 *
 */
@Slf4j
public class EmulatedComputerImpl extends EmulatedComputer {
	private static final Field rootMountField;

	static {
		Field f;

		try {
			f = EmulatedComputer.class.getSuperclass().getDeclaredField("m_rootMount");
			f.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			f = null;
			log.error("Failed to get computer root mount field", e);
		}

		rootMountField = f;
	}

	/**
	 * A class used to create new <code>EmulatedComputer</code> instances
	 *
	 * @author apemanzilla
	 *
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
		@Override
		public EmulatedComputer build() {
			if (!built) {
				EmulatedComputer ec = new EmulatedComputerImpl(env, term, Optional.ofNullable(id).orElse(-1));
				ec.assignID();

				if (label != null)
					ec.setLabel(label);

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
	 * Gets a new builder to create an <code>EmulatedComputer</code> instance
	 *
	 * @return
	 */
	public static Builder builder(IComputerEnvironment env, EmulatedTerminal term) {
		return new BuilderImpl(env, term);
	}

	/**
	 * The <code>EmulatedTerminal</code> that this computer draws to
	 */
	public final EmulatedTerminal terminal;

	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

	private EmulatedComputerImpl(IComputerEnvironment environment, EmulatedTerminal terminal, int id) {
		super(environment, terminal, id);
		this.terminal = terminal;
	}

	@Override
	public boolean addListener(Listener l) {
		return listeners.add(l);
	}

	@Override
	public boolean removeListener(Listener l) {
		return listeners.remove(l);
	}

	@Override
	public void advance(double dt) {
		super.advance(dt);

		for(int i = 0; i < 6; i++) {
			IPeripheral peripheral = getPeripheral(i);
			if (peripheral instanceof Listener) ((Listener) peripheral).onAdvance(dt);
		}

		listeners.forEach(l -> l.onAdvance(dt));
	}

	@Override
	public void copyFiles(Iterable<File> files, String location) throws IOException {
		val mount = this.getRootMount();
		val base = Paths.get(location);

		for (val f : files) {
			val path = base.resolve(f.getName()).toString();

			if (f.isFile()) {
				if (f.length() > mount.getRemainingSpace())
					throw new IOException("Not enough space on computer");

				val s = mount.openForWrite(path);
				IOUtils.copy(FileUtils.openInputStream(f), s);
			} else {
				mount.makeDirectory(path);
				copyFiles(Arrays.asList(f.listFiles()), path);
			}
		}
	}
}
