package net.clgd.ccemux.init;

import java.nio.file.Path;

import dan200.computercraft.ComputerCraft;
import net.clgd.ccemux.api.config.ConfigProperty;

public class UserConfigCCTweaked extends UserConfig {
	private final ConfigProperty<Boolean> debugEnabled = property("debugEnable", boolean.class, false)
		.setName("Enable debug library")
		.setDescription("Enable Lua's debug library.");

	private final ConfigProperty<Integer> computerThreads = property("computerThreads", int.class, 1)
		.setName("Computer thread count")
		.setDescription("Set the number of threads computers can run on. A higher number means more computers can run at once, but may induce lag.\n" +
			"Please note that some mods may not work with a thread count higher than 1. Use with caution.");

	public UserConfigCCTweaked(Path dataDir) {
		super(dataDir);
	}

	@Override
	public void setup() {
		super.setup();

		debugEnabled.addAndFireListener((o, n) -> ComputerCraft.debug_enable = n);
		computerThreads.addAndFireListener((o, n) -> ComputerCraft.computer_threads = n);
	}
}
