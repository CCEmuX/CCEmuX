package net.clgd.ccemux;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.eclipse.xtext.xbase.lib.Pure;

// written in Java because xtend doesn't seem to support complex enums
public enum OperatingSystem {
	Windows(Paths.get(Objects.toString(System.getenv("appdata"), System.getProperty("user.home")))),
	MacOSX(Paths.get(System.getProperty("user.home")).resolve("Library/Application Support")),
	Linux(Paths.get(System.getProperty("user.home")).resolve(".local/share")),
	Other(Paths.get(System.getProperty("user.home")));

	public Path getAppDataDir() {
		return appDataDir;
	}

	private final Path appDataDir;

	OperatingSystem(Path appDataDir) {
		this.appDataDir = appDataDir;
	}

	@Pure
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
}
