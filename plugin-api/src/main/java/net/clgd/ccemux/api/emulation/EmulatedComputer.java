package net.clgd.ccemux.api.emulation;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.core.ComputerContext;
import dan200.computercraft.core.apis.transfer.TransferredFiles;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.ComputerEnvironment;

public abstract class EmulatedComputer extends Computer {
	/**
	 * A listener that's triggered every time the computer is ticked
	 */
	public interface Listener {
		/**
		 * Called when the computer is ticked
		 */
		void onAdvance(double dt);
	}

	/**
	 * A builder used to specify values for an emulated computer before it's created
	 */
	public interface Builder {

		/**
		 * Sets the ID of the computer to construct. Setting the id to {@code null}
		 * (the default value) will result in the ID being automatically chosen by the
		 * environment.
		 *
		 * @return This builder, for chaining
		 */
		@Nonnull
		Builder id(@Nullable Integer num);

		/**
		 * Sets the root ({@code /}) mount of the computer to construct. Setting
		 * the root mount to {@code null} (the default value) will result in one
		 * being created by the environment.
		 *
		 * @param rootMount A factory which constructs the writable mount to use as a root mount. This may be
		 *                  {@code null}, or return {@code null}.
		 * @return This builder, for chaining
		 */
		@Nonnull
		Builder rootMount(@Nullable Supplier<WritableMount> rootMount);

		/**
		 * Sets the root ({@code /}) mount of the computer to construct. Setting
		 * the root mount to {@code null} (the default value) will result in one
		 * being created by the environment.
		 *
		 * @param rootMount The writable mount to use as a root mount.
		 * @return This builder, for chaining
		 * @deprecated Use the {@link #rootMount(Supplier)} override.
		 */
		@Nonnull
		@Deprecated
		default Builder rootMount(@Nullable WritableMount rootMount) {
			return rootMount(() -> rootMount);
		}

		/**
		 * Sets the label of the computer to construct. Setting the label to
		 * {@code null} (the default value) will result in no label being set.
		 *
		 * @return This builder, for chaining
		 */
		@Nonnull
		Builder label(@Nullable String label);

		/**
		 * Set the dimension of the computer's terminal. This overrides {@link EmuConfig#termWidth} and
		 * {@link EmuConfig#termHeight}
		 *
		 * @param width  The new width.
		 * @param height The new height.
		 * @return This builder, for chaining.
		 */
		Builder termSize(int width, int height);

		/**
		 * Set the scale for this computer's terminal. This overrides {@link EmuConfig#termScale}.
		 *
		 * @param scale The new scale.
		 * @return This builder, for chaining.
		 */
		Builder termScale(double scale);

		/**
		 * Builds an emulated computer using the given values
		 */
		@Nonnull
		EmulatedComputer build();
	}

	/**
	 * The terminal used by this computer
	 */
	@Nonnull
	public final EmulatedTerminal terminal;

	protected EmulatedComputer(@Nonnull ComputerContext context, @Nonnull ComputerEnvironment environment, @Nonnull EmulatedTerminal terminal, int id) {
		super(context, environment, terminal, id);
		this.terminal = terminal;
	}

	/**
	 * Adds a listener that will be invoked when ticking this computer
	 */
	public abstract boolean addListener(@Nonnull Listener l);

	/**
	 * Removes a listener
	 *
	 * @see #addListener(Listener)
	 */
	public abstract boolean removeListener(@Nonnull Listener l);

	/**
	 * Transfer a list of files onto the computer, queuing a {@link TransferredFiles#EVENT {@code file_transfer}} event.
	 * <p>
	 * Unlike {@link #copyFiles(Iterable, String)}, this does not visit or copy directories. Only the selected files
	 * will be transferred to the computer.
	 *
	 * @param files The files to copy
	 */
	public abstract void transferFiles(@Nonnull Iterable<File> files) throws IOException;

	/**
	 * Copies the given files into this computer's root mount at the given location.
	 * All files will be copied into the destination regardless of their absolute
	 * path, with their original name. Directories will be recursively copied into
	 * the destination in a similar fashion to files.
	 *
	 * @param files The files to copy
	 */
	public abstract void copyFiles(@Nonnull Iterable<File> files, @Nonnull String location) throws IOException;

	/**
	 * Take a screenshot of the current terminal, saving it to a file.
	 *
	 * @return A future which contains the path to the saved screenshot, or throws an {@link IOException}. The files is
	 * guranteed to be a {@code .png} file.
	 */
	@Nonnull
	public abstract ListenableFuture<File> screenshot();

	/**
	 * Get the terminal scale for renderers to use.
	 *
	 * @return The terminal scale.
	 */
	public abstract double getTermScale();

	/**
	 * Queues a key event
	 */
	public void pressKey(int keycode, boolean repeat) {
		queueEvent("key", new Object[]{keycode, repeat});
	}

	/**
	 * Queues a key up event
	 */
	public void releaseKey(int keycode) {
		queueEvent("key_up", new Object[]{keycode});
	}

	/**
	 * Queues a char event
	 */
	public void pressChar(char c) {
		queueEvent("char", new Object[]{"" + c});
	}

	/**
	 * Queues a paste event
	 *
	 * Note, this uses the same restrictions that CC imposes on pasting: it strips the string to the first
	 * line of input and remove several invalid characters. Use {@link #queueEvent(String, Object[])} if you
	 * need to paste arbitrary text.
	 */
	public void paste(String clipboard) {
		// Clip to the first occurrence of \r or \n.
		int newLineIndex = clipboard.indexOf('\r');
		int returnIndex = clipboard.indexOf('\n');
		if (newLineIndex >= 0 && returnIndex >= 0) {
			clipboard = clipboard.substring(0, Math.min(newLineIndex, returnIndex));
		} else if (newLineIndex >= 0) {
			clipboard = clipboard.substring(0, newLineIndex);
		} else if (returnIndex >= 0) {
			clipboard = clipboard.substring(0, returnIndex);
		}

		// Filter the string: We allow everything greater than a space except the section signal (00a7) and
		// delete (007f).
		clipboard = clipboard.replaceAll("[\0-\31\u00a7\u007F]", "");
		if (clipboard.isEmpty()) return;

		// Clip to 512 characters and queue.
		if (clipboard.length() > 512) clipboard = clipboard.substring(0, 512);
		queueEvent("paste", new Object[]{clipboard});
	}

	/**
	 * Queues a terminate event
	 */
	public void terminate() {
		queueEvent("terminate", new Object[]{});
	}

	/**
	 * Queues a mouse click event
	 */
	public void click(int button, int x, int y, boolean release) {
		if (inTerminal(x, y)) queueEvent(release ? "mouse_up" : "mouse_click", new Object[]{button, x, y});
	}

	/**
	 * Queues a mouse drag event
	 */
	public void drag(int button, int x, int y) {
		if (inTerminal(x, y)) queueEvent("mouse_drag", new Object[]{button, x, y});
	}

	/**
	 * Queues a mouse scroll event
	 */
	public void scroll(int lines, int x, int y) {
		if (inTerminal(x, y)) queueEvent("mouse_scroll", new Object[]{lines, x, y});
	}

	private boolean inTerminal(int x, int y) {
		return x >= 1 && x <= terminal.getWidth() && y >= 1 && y <= terminal.getHeight();
	}
}
