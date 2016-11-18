package net.clgd.ccemux.emulation

object CCEmuXConsts {
	fun getVersion() = Config.package.implementationVersion ?: "[Unknown]"

	fun getGlobalCursorBlink() = System.currentTimeMillis() / 400 % 2 == 0L
}
