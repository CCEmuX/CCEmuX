package net.clgd.ccemux.api.emulation.filesystem;

/**
 * A base type for {@link VirtualFile} and {@link VirtualDirectory}, used by
 * {@link VirtualMount}. Should not be extended by external code.
 *
 * @author apemanzilla
 */
public abstract class VirtualMountEntry {
	VirtualMountEntry() {}
}
