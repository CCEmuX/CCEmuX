package net.clgd.ccemux.emulation.tror

import org.eclipse.xtend.lib.annotations.Data

@Data
class IntPair {
	val int x
	val int y
	
	override toString() {
		x + "," + y
	}
}