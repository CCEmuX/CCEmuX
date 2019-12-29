package net.clgd.ccemux.api.emulation.filesystem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Streams;

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
		 * @throws IllegalArgumentException Thrown if the given path is invalid
		 * @deprecated Use the {@link #addEntry(String, VirtualMountEntry)} version instead.
		 */
		@Deprecated
		public void addEntry(@Nonnull Path path, @Nonnull VirtualMountEntry entry) {
			addEntry(Streams.stream(path.normalize().iterator())
				.map(x -> x.getFileName().toString())
				.collect(Collectors.joining("/")), entry);
		}

		/**
		 * Adds an entry to the directory being built, creating directories and
		 * overwriting existing entries as necessary.
		 *
		 * @throws IllegalArgumentException Thrown if the given path is invalid
		 */
		public void addEntry(@Nonnull String path, @Nonnull VirtualMountEntry entry) {
			if (dir == null) throw new IllegalStateException("Cannot extend already built directory");

			while (path.startsWith("/")) path = path.substring(1);
			if (path.isEmpty()) throw new IllegalArgumentException("Cannot add an entry with an empty path");

			VirtualDirectory current = dir;
			int lastIndex = 0;
			while (true) {
				int nextIndex = path.indexOf('/', lastIndex);
				if (nextIndex >= 0) {
					String name = path.substring(lastIndex, nextIndex);
					VirtualMountEntry next = current.getEntry(name);

					if (!(next instanceof VirtualDirectory)) {
						current.children.put(name, next = new VirtualDirectory());
					}
					current = (VirtualDirectory) next;
					lastIndex = nextIndex + 1;
				} else {
					current.children.put(path.substring(lastIndex), entry);
					return;
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
	 * @param children The entries contained in this directory
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return children.equals(((VirtualDirectory) o).children);
	}

	@Override
	public int hashCode() {
		return children.hashCode();
	}
}
