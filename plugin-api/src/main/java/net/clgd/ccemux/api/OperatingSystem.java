package net.clgd.ccemux.api;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Used to determine which OS is in use and where application data should be
 * stored
 */
public enum OperatingSystem {
	Windows(Paths.get(Objects.toString(System.getenv("appdata"), System.getProperty("user.home")))),
	MacOSX(Paths.get(System.getProperty("user.home")).resolve("Library/Application Support")),
	Linux(Paths.get(System.getProperty("user.home")).resolve(".local/share")),
	Other(Paths.get(System.getProperty("user.home")));

	/**
	 * Gets the directory that should be used for storing application data for this
	 * OS
	 *
	 * @return The directory that application data should be stored in
	 */
	@Nonnull
	public Path getAppDataDir() {
		return appDataDir;
	}

	private final Path appDataDir;

	OperatingSystem(Path appDataDir) {
		this.appDataDir = appDataDir;
	}

	/**
	 * Gets the OS this program is running on
	 *
	 * @return The appropriate value for this OS
	 */
	@Nonnull
	public static OperatingSystem get() {
		String name = System.getProperty("os.name");
		if (name.startsWith("Windows")) {
			return Windows;
		} else if (name.startsWith("Linux")) {
			return Linux;
		} else if (name.startsWith("Mac")) {
			return MacOSX;
		} else {
			return Other;
		}
	}
}
