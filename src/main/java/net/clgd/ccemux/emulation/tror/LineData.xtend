package net.clgd.ccemux.emulation.tror

import org.eclipse.xtend.lib.annotations.Accessors

class LineData {
	@Accessors(PUBLIC_GETTER) val String text
	@Accessors(PUBLIC_GETTER) val String fg
	@Accessors(PUBLIC_GETTER) val String bg
	
	public new(String text, String fg, String bg) {
		this.text = text
		this.fg = fg
		this.bg = bg
	}
	
	override toString() {
		fg + ',' + bg + ',' + text
	}
}