package net.clgd.ccemux.api.emulation.filesystem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;

/**
 * Represents an immutable, in-memory directory for use with
 * {@link VirtualMount}
 * 
 * @author apemanzilla
 *
 */
@EqualsAndHashCode(callSuper=false)
public final class VirtualDirectory extends VirtualMountEntry {
	/**
	 * A builder for {@link VirtualDirectory} that automatically creates the
	 * necessary directories for each entry
	 * 
	 * @author apemanzilla
	 *
	 */
	public static class Builder {
		private VirtualDirectory dir = new VirtualDirectory();

		/**
		 * Adds an entry to the directory being built, creating directories and
		 * overwriting existing entries as necessary.
		 * 
		 * @throws IllegalArgumentException
		 *             Thrown if the given path is invalid
		 */
		public void addEntry(Path path, VirtualMountEntry entry) {
			path = path.normalize();

			if (path.getNameCount() == 0) {
				throw new IllegalArgumentException("Invalid path");
			} else if (path.getNameCount() == 1) {
				dir.children.put(path.getFileName().toString(), entry);
			} else {
				VirtualDirectory current = dir;

				Iterator<Path> i = path.iterator();
				Path p;
				while ((p = i.next()) != null) {
					String name = p.getFileName().toString();
					if (i.hasNext()) {
						if (current.hasEntry(name) && current.getEntry(name) instanceof VirtualDirectory) {
							current = (VirtualDirectory) current.getEntry(name);
						} else {
							VirtualDirectory next = new VirtualDirectory();
							current.children.put(name, next);
							current = next;
						}
					} else {
						current.children.put(name, entry);
						break;
					}
				}
			}
		}

		public VirtualDirectory build() {
			if (dir == null) throw new IllegalStateException("Builder has already been used!");
			VirtualDirectory out = dir;
			dir = null;
			return out;
		}
	}

	private final Map<String, VirtualMountEntry> children;

	/**
	 * Creates a new directory
	 * 
	 * @param children
	 *            The entries contained in this directory
	 */
	public VirtualDirectory(Map<String, VirtualMountEntry> children) {
		this.children = children;
	}

	/**
	 * Creates a new directory with no children
	 */
	public VirtualDirectory() {
		this.children = new HashMap<>();
	}

	public boolean hasEntry(String name) {
		return children.containsKey(name);
	}

	public VirtualMountEntry getEntry(String name) {
		return children.get(name);
	}

	public Set<String> getEntryNames() {
		return children.keySet();
	}
}
