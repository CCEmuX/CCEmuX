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
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof VirtualFile)) return false;
		final VirtualFile other = (VirtualFile) o;
		if (!other.canEqual((Object) this)) return false;
		if (!Arrays.equals(this.getData(), other.getData())) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof VirtualFile;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + Arrays.hashCode(this.getData());
		return result;
	}

	public byte[] getData() {
		return this.data;
	}
}
