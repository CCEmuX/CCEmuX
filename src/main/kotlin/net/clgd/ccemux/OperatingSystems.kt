package net.clgd.ccemux;

import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

enum class OperatingSystems(val appDataDir: Path) {
	Windows(Paths.get(Objects.toString(System.getenv("appdata"), System.getProperty("user.home")))),
	MacOSX(Paths.get(System.getProperty("user.home")).resolve("Library/Application Support")),
	Linux(Paths.get(System.getProperty("user.home")).resolve(".local/share")),
	Other(Paths.get(System.getProperty("user.home")))
}
