package net.clgd.ccemux.plugins.hooks;

import java.nio.file.Path;

import net.clgd.ccemux.api.emulation.Emulator;
import net.clgd.ccemux.api.emulation.filesystem.VirtualDirectory;
import net.clgd.ccemux.api.emulation.filesystem.VirtualMountEntry;

/**
 * Called when CCEmuX creates the ROM for computers. This hook allows plugins to
 * add their own ROM entries, using {@link VirtualDirectory.Builder}.
 * 
 * @author apemanzilla
 * @see VirtualDirectory.Builder#addEntry(Path, VirtualMountEntry)
 */
@FunctionalInterface
public interface CreatingROM extends Hook {
	public void onCreatingROM(Emulator emu, VirtualDirectory.Builder romBuilder);
}
