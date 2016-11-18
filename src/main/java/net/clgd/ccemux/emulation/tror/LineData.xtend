package net.clgd.ccemux.emulation.tror

import org.eclipse.xtend.lib.annotations.Data

@Data
class LineData {
	val String text
	val String fg
	val String bg
	
	override toString() {
		fg + ',' + bg + ',' + text
	}
}