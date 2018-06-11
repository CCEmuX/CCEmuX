package net.clgd.ccemux.api.emulation.filesystem;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.annotation.Nonnull;

/**
 * Represents an immutable, in-memory file for use with
 * {@link VirtualMount}
 *
 * @author apemanzilla
 */
public final class VirtualFile extends VirtualMountEntry {
	private final byte[] data;

	public VirtualFile(@Nonnull byte[] data) {
		this.data = data;
	}

	public VirtualFile(@Nonnull char[] data) {
		this(new String(data));
	}

	public VirtualFile(@Nonnull String data) {
		this(data.getBytes(StandardCharsets.UTF_8));
	}

	public int length() {
		return data.length;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VirtualFile that = (VirtualFile) o;
		return Arrays.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	@Nonnull
	public byte[] getData() {
		return this.data;
	}
}
