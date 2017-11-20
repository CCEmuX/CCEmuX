package net.clgd.ccemux.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * A simple utility to get the operating system and appropriate data directory
 */
public enum OperatingSystem {
	Windows(Paths.get(Objects.toString(System.getenv("appdata"), System.getProperty("user.home")))),
	MacOSX(Paths.get(System.getProperty("user.home")).resolve("Library/Application Support")),
	Linux(Paths.get(System.getProperty("user.home")).resolve(".local/share")),
	Other(Paths.get(System.getProperty("user.home")));

	/**
	 * Gets the appropriate data directory for this operating system
	 */
	public Path getAppDataDir() {
		return appDataDir;
	}

	private final Path appDataDir;

	OperatingSystem(Path appDataDir) {
		this.appDataDir = appDataDir;
	}

	public static OperatingSystem get() {
		String name = System.getProperty("os.name");
		if (name.startsWith("Windows"))
			return Windows;
		else if (name.startsWith("Linux"))
			return Linux;
		else if (name.startsWith("Mac"))
			return MacOSX;
		else
			return Other;
	}

	public static boolean isWindows() {
		return get().equals(Windows);
	}

	public static boolean isMacOSX() {
		return get().equals(MacOSX);
	}

	public static boolean isLinux() {
		return get().equals(Linux);
	}
}
