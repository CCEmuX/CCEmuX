package net.clgd.ccemux.emulation.tror

import org.eclipse.xtend.lib.annotations.Accessors

class IntPair {
	@Accessors(PUBLIC_GETTER) val int x
	@Accessors(PUBLIC_GETTER) val int y
	
	public new(int x, int y) {
		this.x = x
		this.y = y
	}
	
	override toString() {
		x + "," + y
	}
}