package net.clgd.ccemux.rendering.tror;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import net.clgd.ccemux.api.emulation.EmulatedComputer;

public class InputProvider {
	private static InputProvider stdinProvider;
	private static final Object lock = new Object();

	private final Map<Integer, BlockingDeque<InputPacket>> events = new HashMap<>();
	private final Thread thread;

	public InputProvider(InputStream stream) {
		thread = new Thread(() -> {
			try (Scanner scanner = new Scanner(stream, "UTF-8")) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					int metaStart = line.indexOf(':');
					int metaEnd = line.indexOf(';');

					if (metaStart < 0 || metaEnd < 0 || metaEnd < metaStart) continue;

					String code = line.substring(0, metaStart);
					String meta = line.substring(metaStart + 1, metaEnd);
					String payload = line.substring(metaEnd + 1);

					int computer;
					try {
						computer = Integer.parseInt(meta);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						continue;
					}

					getQueue(computer).add(new InputPacket(code, payload));
				}
			}
		});
		thread.setName("TRoR input provider");
		thread.setDaemon(true);
		thread.start();
	}

	BlockingDeque<InputPacket> getQueue(EmulatedComputer computer) {
		return getQueue(computer.getID());
	}

	private BlockingDeque<InputPacket> getQueue(int id) {
		synchronized (events) {
			BlockingDeque<InputPacket> queue = events.get(id);
			if (queue == null) events.put(id, queue = new LinkedBlockingDeque<>());
			return queue;
		}
	}

	public static InputProvider getStdinProvider() {
		if (stdinProvider == null) {
			synchronized (lock) {
				if (stdinProvider == null) stdinProvider = new InputProvider(System.in);
			}
		}

		return stdinProvider;
	}

	static class InputPacket {
		final String code;
		final String payload;

		InputPacket(String code, String payload) {
			this.code = code;
			this.payload = payload;
		}
	}
}
