package net.clgd.ccemux.emulation;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerEnvironment;

/**
 * Represents a computer that can be emulated via CCEmuX
 * 
 * @author apemanzilla
 *
 */
public class EmulatedComputer extends Computer {
	private static final Logger log = LoggerFactory.getLogger(EmulatedComputer.class);

	/**
	 * A class used to create new <code>EmulatedComputer</code> instances
	 * 
	 * @author apemanzilla
	 *
	 */
	public static class Builder {
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
}
