package net.clgd.ccemux.api.emulation.filesystem;

import java.nio.charset.StandardCharsets;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents an immutable, in-memory file for use with
 * {@link VirtualMount}
 * 
 * @author apemanzilla
 *
 */
@EqualsAndHashCode(callSuper=false)
public final class VirtualFile extends VirtualMountEntry {
	@Getter
	private final byte[] data;

	public VirtualFile(byte[] data) {
		this.data = data;
	}

	public VirtualFile(char[] data) {
		this(new String(data));
	}

	public VirtualFile(String data) {
		this(data.getBytes(StandardCharsets.UTF_8));
	}

	public int length() {
		return data.length;
	}
}
