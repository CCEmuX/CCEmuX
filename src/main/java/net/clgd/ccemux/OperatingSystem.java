package net.clgd.ccemux;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.eclipse.xtext.xbase.lib.Pure;

// written in Java because xtend doesn't seem to support complex enums
public enum OperatingSystem {
	Windows {
		@Override
		public Path getAppDataDir() {
			return Paths.get(Objects.toString(System.getenv("appdata"), System.getProperty("user.home")));
		}
	},
	MacOSX {
		@Override
		public Path getAppDataDir() {
			return Paths.get(System.getProperty("user.home")).resolve("Library/Application Support");
		}
	},
	Linux {
		@Override
		public Path getAppDataDir() {
			return Paths.get(System.getProperty("user.home")).resolve(".local/share");
		}
	},
	Other {
		@Override
		public Path getAppDataDir() {
			return Paths.get(System.getProperty("user.home"));
		}
	};
	
	public abstract Path getAppDataDir();
	
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
