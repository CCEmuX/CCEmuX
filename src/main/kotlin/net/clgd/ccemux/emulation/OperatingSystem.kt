package net.clgd.ccemux.emulation

import net.clgd.ccemux.OperatingSystems

object OperatingSystem {
	fun get(): OperatingSystems {
		val name = System.getProperty("os.name");
		return when {
			name.startsWith("Windows") 	-> OperatingSystems.Windows
			name.startsWith("Linux") 	-> OperatingSystems.Linux
			name.startsWith("Mac") 		-> OperatingSystems.MacOSX
			else 						-> OperatingSystems.Other
		}
	}
}
