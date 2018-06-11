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
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VirtualFile other = (VirtualFile) obj;
		if (!Arrays.equals(data, other.data)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		return result;
	}

	public byte[] getData() {
		return this.data;
	}
}
