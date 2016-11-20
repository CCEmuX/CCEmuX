package net.clgd.ccemux.rendering.tror;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

class AsyncScanner implements Runnable {
	private final Scanner input;
	private ArrayList<String> lines = new ArrayList<>();

	public AsyncScanner(InputStream input) {
		this.input = new Scanner(input);
	}

	public boolean hasLines() {
		return lines.size() > 0;
	}

	public ArrayList<String> getLines() {
		ArrayList<String> out = new ArrayList<>(lines);
		lines.clear();
		return out;
	}

	public Thread start() {
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
		return t;
	}

	@Override
	public void run() {
		while (input.hasNextLine()) {
			lines.add(input.nextLine());
		}
	}
}
