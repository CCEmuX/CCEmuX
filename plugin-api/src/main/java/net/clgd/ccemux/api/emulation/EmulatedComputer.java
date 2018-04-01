package net.clgd.ccemux.api.emulation;

import java.io.File;
import java.io.IOException;

import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerEnvironment;

public abstract class EmulatedComputer extends Computer {
	/**
	 * A listener that's triggered every time the computer is ticked
	 *
	 */
	public static interface Listener {
		/**
		 * Called when the computer is ticked
		 */
		public void onAdvance(double dt);
	}

	/**
	 * A builder used to specify values for an emulated computer before it's created
	 */
	public static interface Builder {

		/**
		 * Sets the ID of the computer to construct. Setting the id to <code>null</code>
		 * (the default value) will result in the ID being automatically chosen by the
		 * environment.
		 *
		 * @return This builder, for chaining
		 */
		public Builder id(Integer num);

		/**
		 * Sets the root (<code>/</code>) mount of the computer to construct. Setting
		 * the root mount to <code>null</code> (the default value) will result in one
		 * being created by the environment.
		 *
		 * @param rootMount
		 *            The writable mount to use as a root mount
		 * @return This builder, for chaining
		 */
		public Builder rootMount(IWritableMount rootMount);

		/**
		 * Sets the label of the computer to construct. Setting the label to
		 * <code>null</code> (the default value) will result in no label being set.
		 *
		 * @return This builder, for chaining
		 */
		public Builder label(String label);

		/**
		 * Builds an emulated computer using the given values
		 */
		public EmulatedComputer build();

	}

	/**
	 * The terminal used by this computer
	 */
	public final EmulatedTerminal terminal;

	protected EmulatedComputer(IComputerEnvironment environment, EmulatedTerminal terminal, int id) {
		super(environment, terminal, id);

		this.terminal = terminal;
	}

	/**
	 * Adds a listener that will be invoked when ticking this computer
	 */
	public abstract boolean addListener(Listener l);

	/**
	 * Removes a listener
	 * 
	 * @see #addListener(Listener)
	 */
	public abstract boolean removeListener(Listener l);

	/**
	 * Copies the given files into this computer's root mount at the given location.
	 * All files will be copied into the destination regardless of their absolute
	 * path, with their original name. Directories will be recursively copied into
	 * the destination in a similar fashion to files.
	 *
	 * @param files
	 *            The files to copy
	 */
	public abstract void copyFiles(Iterable<File> files, String location) throws IOException;

	/**
	 * Queues a key event
	 */
	public void pressKey(int keycode, boolean repeat) {
		queueEvent("key", new Object[] { keycode, repeat });
	}

	/**
	 * Queues a key up event
	 */
	public void releaseKey(int keycode) {
		queueEvent("key_up", new Object[] { keycode });
	}

	/**
	 * Queues a char event
	 */
	public void pressChar(char c) {
		queueEvent("char", new Object[] { "" + c });
	}

	/**
	 * Queues a paste event
	 */
	public void paste(String text) {
		queueEvent("paste", new Object[] { text });
	}

	/**
	 * Queues a terminate event
	 */
	public void terminate() {
		queueEvent("terminate", new Object[] {});
	}

	/**
	 * Queues a mouse click event
	 */
	public void click(int button, int x, int y, boolean release) {
		queueEvent(release ? "mouse_up" : "mouse_click", new Object[] { button, x, y });
	}

	/**
	 * Queues a mouse drag event
	 */
	public void drag(int button, int x, int y) {
		queueEvent("mouse_drag", new Object[] { button, x, y });
	}

	/**
	 * Queues a mouse scroll event
	 */
	public void scroll(int lines, int x, int y) {
		queueEvent("mouse_scroll", new Object[] { lines, x, y });
	}
}