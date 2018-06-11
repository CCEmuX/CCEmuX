package net.clgd.ccemux.api.emulation.filesystem;

import java.nio.charset.StandardCharsets;

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
	@SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof VirtualFile)) return false;
		final VirtualFile other = (VirtualFile) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (!java.util.Arrays.equals(this.getData(), other.getData())) return false;
		return true;
	}

	@SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof VirtualFile;
	}

	@Override
	@SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + java.util.Arrays.hashCode(this.getData());
		return result;
	}

	@SuppressWarnings("all")
	public byte[] getData() {
		return this.data;
	}
}
