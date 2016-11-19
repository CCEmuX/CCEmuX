package net.clgd.ccemux.emulation;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.filesystem.ComboMount;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.JarMount;

public class EmulatedEnvironment implements IComputerEnvironment {

	private int nextID = 0;
	private final CCEmuX emu;
	
	EmulatedEnvironment(CCEmuX emu) {
		this.emu = emu;
	}
	
	@Override
	public int assignNewID() {
		return nextID++;
	}
	
	@Override
	public IMount createResourceMount(String domain, String subPath) {
		String path = Paths.get("assets", domain, subPath).toString().replace('\\', '/');
		
		if (path.startsWith("\\")) path = path.substring(1);
		try {
			return new ComboMount(new IMount[] {
				new JarMount(emu.ccJar, path),
				new CustomRomMount(new ZipInputStream(this.getClass().getResourceAsStream("/custom.rom")))
			});
		} catch (IOException e) {
			emu.logger.error("Failed to create resource mount", e);
			return null;
		}
	}
	
	@Override
	public IWritableMount createSaveDirMount(String path, long capacity) {
		return new FileMount(emu.dataDir.resolve(path).toFile(), capacity);
	}
	
	@Override
	public long getComputerSpaceLimit() {
		return Long.MAX_VALUE;
	}
	
	@Override
	public int getDay() {
		return (int)((emu.getTicksSinceStart() + 6000L) / 24000L) + 1;
	}
	
	@Override
	public double getTimeOfDay() {
		return ((emu.getTicksSinceStart() + 6000) % 24000) / 1000;
	}
	
	@Override
	public boolean isColour() {
		return true;
	}
	
	@Override
	public String getHostString() {
		return "ComputerCraft " + ComputerCraft.getVersion() + " (CCEmuX v" + CCEmuX.getVersion() + ")";
	}
}
