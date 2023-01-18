package net.clgd.ccemux.api.emulation.filesystem;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Streams;
import dan200.computercraft.api.filesystem.FileOperationException;
import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.core.apis.handles.ArrayByteChannel;

/**
 * An immutable, in-memory {@link dan200.computercraft.api.filesystem.Mount
 * IMount} implementation.
 *
 * @author apemanzilla
 */
public class VirtualMount implements Mount {
	private final VirtualDirectory root;

	/**
	 * Constructs a new {@link VirtualMount} with the given
	 * {@link net.clgd.ccemux.api.emulation.filesystem.VirtualDirectory
	 * VirtualDirectory} as its root folder.
	 *
	 * @param root The root directory for this virtual mount
	 */
	public VirtualMount(@Nonnull VirtualDirectory root) {
		this.root = root;
	}

	/**
	 * Follows the given path to get a
	 * {@link net.clgd.ccemux.api.emulation.filesystem.VirtualMountEntry
	 * MountEntry}. Returns {@code null} if the path is invalid (e.g.
	 * non-existent entry or trying to get child of a directory)
	 *
	 * @param path The path to follow
	 * @return The entry at the given path, or {@code null} if the path is
	 * invalid
	 * @deprecated Use {@link #follow(String)} instead.
	 */
	@Nullable
	@Deprecated
	public VirtualMountEntry follow(@Nonnull Path path) {
		return follow(Streams.stream(path.normalize().iterator())
			.map(x -> x.getFileName().toString())
			.collect(Collectors.joining("/")));
	}

	/**
	 * Follows the given path to get a
	 * {@link net.clgd.ccemux.api.emulation.filesystem.VirtualMountEntry
	 * MountEntry}. Returns {@code null} if the path is invalid (e.g.
	 * non-existent entry or trying to get child of a directory)
	 *
	 * @param path The path to follow.
	 * @return The entry at the given path, or {@code null} if the path is
	 * invalid
	 */
	public VirtualMountEntry follow(@Nonnull String path) {
		VirtualMountEntry current = getRoot();
		if (path.isEmpty()) return current;

		int lastIndex = 0;
		while (true) {
			int nextIndex = path.indexOf('/', lastIndex);
			if (!(current instanceof VirtualDirectory)) return null;
			if (nextIndex == -1) {
				return ((VirtualDirectory) current).getEntry(path.substring(lastIndex));
			} else {
				current = ((VirtualDirectory) current).getEntry(path.substring(lastIndex, nextIndex));
				lastIndex = nextIndex + 1;
			}
		}
	}

	@Override
	public boolean exists(@Nonnull String path) {
		return follow(path) != null;
	}

	@Override
	public long getSize(@Nonnull String path) throws IOException {
		VirtualMountEntry e = follow(path);
		if (e == null) throw new FileOperationException("No such file or directory");
		return e instanceof VirtualFile ? ((VirtualFile) e).length() : 0;
	}

	@Override
	public boolean isDirectory(@Nonnull String path) {
		return follow(path) instanceof VirtualDirectory;
	}

	@Override
	public void list(@Nonnull String path, @Nonnull List<String> names) throws IOException {
		VirtualMountEntry e = follow(path);
		if (e instanceof VirtualDirectory) {
			names.addAll(((VirtualDirectory) e).getEntryNames());
		} else {
			throw new FileOperationException("Cannot list children of non-directory");
		}
	}

	@Nonnull
	@Override
	public SeekableByteChannel openForRead(@Nonnull String path) throws IOException {
		VirtualMountEntry e = follow(path);
		if (e instanceof VirtualFile) {
			return new ArrayByteChannel(((VirtualFile) e).getData());
		} else {
			throw new FileOperationException("Only files can be read");
		}
	}

	@Nonnull
	public VirtualDirectory getRoot() {
		return this.root;
	}
}
