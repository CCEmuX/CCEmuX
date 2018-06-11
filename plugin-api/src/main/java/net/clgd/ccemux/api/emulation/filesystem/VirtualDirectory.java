package net.clgd.ccemux.api.emulation.filesystem;

import java.nio.file.Path;
import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an immutable, in-memory directory for use with
 * {@link VirtualMount}
 * 
 * @author apemanzilla
 */
public final class VirtualDirectory extends VirtualMountEntry {

	/**
	 * A builder for {@link VirtualDirectory} that automatically creates the
	 * necessary directories for each entry
	 * 
	 * @author apemanzilla
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
		public void addEntry(@Nonnull Path path, @Nonnull VirtualMountEntry entry) {
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

		@Nonnull
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
	public VirtualDirectory(@Nonnull Map<String, VirtualMountEntry> children) {
		this.children = children;
	}

	/**
	 * Creates a new directory with no children
	 */
	public VirtualDirectory() {
		this.children = new HashMap<>();
	}

	public boolean hasEntry(@Nonnull String name) {
		return children.containsKey(name);
	}

	@Nullable
	public VirtualMountEntry getEntry(@Nonnull String name) {
		return children.get(name);
	}

	@Nonnull
	public Set<String> getEntryNames() {
		return children.keySet();
	}

	@Override
	@SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof VirtualDirectory)) return false;
		final VirtualDirectory other = (VirtualDirectory) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		final java.lang.Object this$children = this.children;
		final java.lang.Object other$children = other.children;
		if (this$children == null ? other$children != null : !this$children.equals(other$children)) return false;
		return true;
	}

	@SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof VirtualDirectory;
	}

	@Override
	@SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final java.lang.Object $children = this.children;
		result = result * PRIME + ($children == null ? 43 : $children.hashCode());
		return result;
	}
}
