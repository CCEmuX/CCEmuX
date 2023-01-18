package net.clgd.ccemux.init;

import java.nio.file.Path;

import dan200.computercraft.core.CoreConfig;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;

public class UserConfigCCTweaked extends UserConfig {
	private final ConfigProperty<Integer> computerThreads = property("computerThreads", int.class, 1)
		.setName("Computer thread count")
		.setDescription("Set the number of threads computers can run on. A higher number means more computers can run at once, but may induce lag.\n" +
			"Please note that some mods may not work with a thread count higher than 1. Use with caution.");

	private final Group http = group("http")
		.setName("HTTP API")
		.setDescription("Additional config options relating to the HTTP API");

	private final ConfigProperty<Boolean> httpWebsocketEnabled = http.property("websocketEnabled", boolean.class, CoreConfig.httpWebsocketEnabled)
		.setName("Enable websockets")
		.setDescription("Enable use of http websockets. This requires \"httpEnable\" to also be true.");

	private final ConfigProperty<Integer> httpMaxRequests = http.property("max_requests", int.class, CoreConfig.httpMaxRequests)
		.setName("Maximum concurrent requests")
		.setDescription("The number of http requests a computer can make at one time. Additional requests will be queued, and sent when the running requests have finished. Set to 0 for unlimited.");

	private final ConfigProperty<Integer> httpMaxWebsockets = http.property("max_websockets", int.class, CoreConfig.httpMaxWebsockets)
		.setName("Maximum concurrent websockets")
		.setDescription("The number of websockets a computer can have open at one time. Set to 0 for unlimited.");

	public UserConfigCCTweaked(Path dataDir, Path assetDir, Path computerDir) {
		super(dataDir, assetDir, computerDir);
	}

	public int getComputerThreads() {
		return computerThreads.get();
	}

	@Override
	public void setup() {
		super.setup();

		httpWebsocketEnabled.addAndFireListener((o, n) -> CoreConfig.httpWebsocketEnabled = n);
		httpMaxRequests.addAndFireListener((o, n) -> CoreConfig.httpMaxRequests = n);
		httpMaxWebsockets.addAndFireListener((o, n) -> CoreConfig.httpMaxWebsockets = n);
	}
}
