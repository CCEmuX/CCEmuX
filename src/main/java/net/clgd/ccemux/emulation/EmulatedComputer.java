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
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerEnvironment;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a computer that can be emulated via CCEmuX
 * 
 * @author apemanzilla
 *
 */
@Slf4j
public class EmulatedComputer extends Computer {	
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
	public static class Builder {
		private final IComputerEnvironment env;
		private final EmulatedTerminal term;

		private Integer id = null;

		private IWritableMount rootMount = null;

		private String label = null;

		private transient boolean built = false;

		private Builder(IComputerEnvironment env, EmulatedTerminal term) {
			this.env = env;
			this.term = term;
		}

		/**
		 * Sets the ID of the computer to construct. Setting the id to
		 * <code>null</code> (the default value) will result in the ID being
		 * automatically chosen by the environment.
		 * 
		 * @return This builder, for chaining
		 */
		public Builder id(Integer num) {
			id = num;
			return this;
		}

		/**
		 * Sets the root (<code>/</code>) mount of the computer to construct.
		 * Setting the root mount to <code>null</code> (the default value) will
		 * result in one being created by the environment.
		 * 
		 * @param rootMount
		 *            The writable mount to use as a root mount
		 * @return This builder, for chaining
		 */
		public Builder rootMount(IWritableMount rootMount) {
			this.rootMount = rootMount;
			return this;
		}

		/**
		 * Sets the label of the computer to construct. Setting the label to
		 * <code>null</code> (the default value) will result in no label being
		 * set.
		 * 
		 * @return This builder, for chaining
		 */
		public Builder label(String label) {
			this.label = label;
			return this;
		}

		public EmulatedComputer build() {
			if (!built) {
				EmulatedComputer ec = new EmulatedComputer(env, term, Optional.ofNullable(id).orElse(-1));
				ec.assignID();

				if (label != null) ec.setLabel(label);

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
				throw new IllegalStateException("This computer has alrady been built!");
			}
		}
	}

	/**
	 * Gets a new builder to create an <code>EmulatedComputer</code> instance
	 * 
	 * @return
	 */
	public static Builder builder(IComputerEnvironment env, EmulatedTerminal term) {
		return new Builder(env, term);
	}

	public static interface Listener {
		public void onAdvance(double dt);
	}

	/**
	 * The <code>EmulatedTerminal</code> that this computer draws to
	 */
	public final EmulatedTerminal terminal;

	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

	private EmulatedComputer(IComputerEnvironment environment, EmulatedTerminal terminal, int id) {
		super(environment, terminal, id);
		this.terminal = terminal;
	}

	public boolean addListener(Listener l) {
		return listeners.add(l);
	}

	public boolean removeListener(Listener l) {
		return listeners.remove(l);
	}

	@Override
	public void advance(double dt) {
		super.advance(dt);

		listeners.forEach(l -> l.onAdvance(dt));
	}

	public void pressKey(int keycode, boolean release) {
		queueEvent(release ? "key_up" : "key", new Object[] { keycode });
	}

	public void pressChar(char c) {
		queueEvent("char", new Object[] { "" + c });
	}

	public void paste(String text) {
		queueEvent("paste", new Object[] { text });
	}

	public void terminate() {
		queueEvent("terminate", new Object[] {});
	}

	public void click(int button, int x, int y, boolean release) {
		queueEvent(release ? "mouse_up" : "mouse_click", new Object[] { button, x, y });
	}

	public void drag(int button, int x, int y) {
		queueEvent("mouse_drag", new Object[] { button, x, y });
	}

	public void scroll(int lines, int x, int y) {
		queueEvent("mouse_scroll", new Object[] { lines, x, y });
	}
	
	/**
	 * Copies the given files into this computer's root mount at the given
	 * location. All files will be copied into the destination regardless of
	 * their absolute path, with their original name. Directories will be
	 * recursively copied into the destination in a similar fashion to files.
	 * 
	 * @param files
	 *            The files to copy
	 */
	public void copyFiles(Iterable<File> files, String location) throws IOException, ReflectiveOperationException {
		if (rootMountField == null) throw new IllegalStateException("No reference to root mount, cannot write files to computer");
		val mount = (IWritableMount) rootMountField.get(this);
		val base = Paths.get(location);
		
		for (val f : files) {
			val path = base.resolve(f.getName()).toString();
			
			if (f.isFile()) {
				val s = mount.openForWrite(path);
				IOUtils.copy(FileUtils.openInputStream(f), s);
			} else {
				mount.makeDirectory(path);
				copyFiles(Arrays.asList(f.listFiles()), path);
			}
		}
	}
}
