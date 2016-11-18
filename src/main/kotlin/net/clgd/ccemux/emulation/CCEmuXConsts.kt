package net.clgd.ccemux.emulation

import net.clgd.ccemux.Config

object CCEmuXConsts {
	fun getVersion() = Config::class.java.`package`.implementationVersion ?: "[Unknown]"

	fun getGlobalCursorBlink() = System.currentTimeMillis() / 400 % 2 == 0L
}
