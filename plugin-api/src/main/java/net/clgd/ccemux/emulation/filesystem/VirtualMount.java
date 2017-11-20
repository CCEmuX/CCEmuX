package net.clgd.ccemux.emulation.filesystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import dan200.computercraft.api.filesystem.IMount;
import lombok.Getter;

/**
 * An immutable, in-memory {@link dan200.computercraft.api.filesystem.IMount
 * IMount} implementation.
 * 
 * @author apemanzilla
 *
 */
public class VirtualMount implements IMount {
	@Getter
	private final VirtualDirectory root;

	/**
	 * Constructs a new <code>VirtualMount</code> with the given
	 * {@link net.clgd.ccemux.emulation.filesystem.VirtualDirectory
	 * VirtualDirectory} as its root folder.
	 * 
	 * @param root
	 */
	public VirtualMount(VirtualDirectory root) {
		this.root = root;
	}

	/**
	 * Follows the given path to get a
	 * {@link net.clgd.ccemux.emulation.filesystem.VirtualMountEntry
	 * MountEntry}. Returns <code>null</code> if the path is invalid (e.g.
	 * non-existent entry or trying to get child of a directory)
	 * 
	 * @param path
	 *            The path to follow
	 * @return The entry at the given path, or <code>null</code> if the path is
	 *         invalid
	 */
	public VirtualMountEntry follow(Path path) {
		if (path.normalize().equals(Paths.get(""))) return root;

		VirtualMountEntry current = getRoot();
		for (Path p : path.normalize()) {
			if (!(current instanceof VirtualDirectory)) return null;
			current = ((VirtualDirectory) current).getEntry(p.getName(0).toString());
			if (current == null) return null;
		}
		return current;
	}

	@Override
	public boolean exists(String path) {
		return follow(Paths.get(path)) != null;
	}

	@Override
	public long getSize(String path) throws IOException {
		VirtualMountEntry e = follow(Paths.get(path));
		if (e == null) throw new IOException("No such file or directory");
		return e instanceof VirtualFile ? ((VirtualFile) e).length() : 0;
	}

	@Override
	public boolean isDirectory(String path) {
		return follow(Paths.get(path)) instanceof VirtualDirectory;
	}

	@Override
	public void list(String path, List<String> names) throws IOException {
		VirtualMountEntry e = follow(Paths.get(path));
		if (e instanceof VirtualDirectory) {
			names.addAll(((VirtualDirectory) e).getEntryNames());
		} else {
			throw new IOException("Only directories can be listed");
		}
	}

	@Override
	public InputStream openForRead(String path) throws IOException {
		VirtualMountEntry e = follow(Paths.get(path));
		if (e instanceof VirtualFile) {
			return new ByteArrayInputStream(((VirtualFile) e).getData());
		} else {
			throw new IOException("Only files can be read");
		}
	}
}
