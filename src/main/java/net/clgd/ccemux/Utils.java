package net.clgd.ccemux;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public final class Utils {
	private static final int RETRIES = 10;
	private static Random random = new Random();

	private Utils() {
	}

	public static synchronized File createUniqueFile(File directory, String name, String extension) throws IOException {
		File file = new File(directory, name + extension);
		if (file.createNewFile()) return file;

		for (int i = 0; i < RETRIES; i++) {
			int id = Math.abs(random.nextInt());
			file = new File(directory, name + "." + id + extension);
			if (file.createNewFile()) return file;
		}

		throw new IOException("Unable to create temporary file");
	}
}
