package net.clgd.ccemux.init;

import java.nio.file.Path;

import dan200.computercraft.ComputerCraft;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;

public class UserConfigCCTweaked extends UserConfig {
	private final ConfigProperty<Boolean> debugEnabled = property("debugEnable", boolean.class, false)
		.setName("Enable debug library")
		.setDescription("Enable Lua's debug library.");

	private final ConfigProperty<Integer> computerThreads = property("computerThreads", int.class, 1)
		.setName("Computer thread count")
		.setDescription("Set the number of threads computers can run on. A higher number means more computers can run at once, but may induce lag.\n" +
			"Please note that some mods may not work with a thread count higher than 1. Use with caution.");

	private final Group http = group("http")
		.setName("HTTP API")
		.setDescription("Additional config options relating to the HTTP API");

	private final ConfigProperty<Boolean> httpWebsocketEnabled = http.property("websocketEnabled", boolean.class, ComputerCraft.http_websocket_enable)
		.setName("Enable websockets")
		.setDescription("Enable use of http websockets. This requires \"httpEnable\" to also be true.");

	private final ConfigProperty<Integer> httpTimeout = http.property("timeout", int.class, ComputerCraft.httpTimeout)
		.setName("Timeout")
		.setDescription("The period of time (in milliseconds) to wait before a HTTP request times out. Set to 0 for unlimited.");

	private final ConfigProperty<Integer> httpMaxRequests = http.property("max_requests", int.class, ComputerCraft.httpMaxRequests)
		.setName("Maximum concurrent requests")
		.setDescription("The number of http requests a computer can make at one time. Additional requests will be queued, and sent when the running requests have finished. Set to 0 for unlimited.");

	private final ConfigProperty<Long> httpMaxDownload = http.property("max_download", long.class, ComputerCraft.httpMaxDownload)
		.setName("Maximum response size")
		.setDescription("The maximum size (in bytes) that a computer can download in a single request. Note that responses may receive more data than allowed, but this data will not be returned to the client.");

	private final ConfigProperty<Long> httpMaxUpload = http.property("max_upload", long.class, ComputerCraft.httpMaxUpload)
		.setName("Maximum request size")
		.setDescription("The maximum size (in bytes) that a computer can upload in a single request. This includes headers and POST text.");

	private final ConfigProperty<Integer> httpMaxWebsockets = http.property("max_websockets", int.class, ComputerCraft.httpMaxWebsockets)
		.setName("Maximum concurrent websockets")
		.setDescription("The number of websockets a computer can have open at one time. Set to 0 for unlimited.");

	private final ConfigProperty<Integer> httpMaxWebsocketMessage = http.property("maxWebsocketMessage", int.class, ComputerCraft.httpMaxWebsocketMessage)
		.setName("Maximum websocket message size")
		.setDescription("The maximum size (in bytes) that a computer can send or receive in one websocket packet.");


	public UserConfigCCTweaked(Path dataDir, Path computerDir) {
		super(dataDir, computerDir);
	}

	@Override
	public void setup() {
		super.setup();

		debugEnabled.addAndFireListener((o, n) -> ComputerCraft.debug_enable = n);
		computerThreads.addAndFireListener((o, n) -> ComputerCraft.computer_threads = n);

		httpWebsocketEnabled.addAndFireListener((o, n) -> ComputerCraft.http_websocket_enable = n);
		httpTimeout.addAndFireListener((o, n) -> ComputerCraft.httpTimeout = n);
		httpMaxRequests.addAndFireListener((o, n) -> ComputerCraft.httpMaxRequests = n);
		httpMaxDownload.addAndFireListener((o, n) -> ComputerCraft.httpMaxDownload = n);
		httpMaxUpload.addAndFireListener((o, n) -> ComputerCraft.httpMaxUpload = n);
		httpMaxWebsockets.addAndFireListener((o, n) -> ComputerCraft.httpMaxWebsockets = n);
		httpMaxWebsocketMessage.addAndFireListener((o, n) -> ComputerCraft.httpMaxWebsocketMessage = n);
	}
}
