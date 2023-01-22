package net.clgd.ccemux.emulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.core.computer.GlobalEnvironment;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.JarMount;
import net.clgd.ccemux.api.emulation.filesystem.VirtualDirectory;
import net.clgd.ccemux.api.emulation.filesystem.VirtualMount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GlobalEnvironmentImpl implements GlobalEnvironment {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalEnvironmentImpl.class);

	private final CCEmuX emu;

	GlobalEnvironmentImpl(CCEmuX emu) {
		this.emu = emu;
	}

	@Override
	public String getHostString() {
		String version = CCEmuX.getVersion();
		String ccVersion = CCEmuX.getCCVersion();
		return String.format("ComputerCraft %s (CCEmuX %s)", ccVersion, version);
	}

	@Override
	public String getUserAgent() {
		return "computercraft/" + CCEmuX.getCCVersion();
	}

	@Override
	public Mount createResourceMount(String domain, String subPath) {
		JarMount jarMount;
		try {
			jarMount = new JarMount(emu.getCCJar(), "data/" + domain + "/" + subPath);
		} catch (IOException e) {
			LOGGER.error("Could not create mount from mod jar", e);
			return null;
		}

		VirtualDirectory.Builder romBuilder = new VirtualDirectory.Builder();
		emu.getPluginMgr().onCreatingROM(emu, romBuilder);

		return new ComboMount(new Mount[]{
			// From data directory
			new FileMount(emu.getConfig().getAssetDir().resolve(Paths.get(domain, subPath))),
			// From plugin files
			new VirtualMount(romBuilder.build()),
			// From ComputerCraft JAR
			jarMount,
		});
	}

	@Override
	public InputStream createResourceFile(String domain, String subPath) {
		File assetFile = emu.getConfig().getAssetDir().resolve(Paths.get(domain, subPath)).toFile();
		if (assetFile.exists() && assetFile.isFile()) {
			try {
				return new FileInputStream(assetFile);
			} catch (FileNotFoundException e) {
				LOGGER.error("Failed to create resource file", e);
			}
		}

		return CCEmuX.class.getClassLoader().getResourceAsStream("data/" + domain + "/" + subPath);
	}
}
