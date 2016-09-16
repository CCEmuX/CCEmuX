package net.clgd.ccemux.terminal

import org.eclipse.xtend.lib.annotations.Data

@Data class TerminalPixel {
	int backgroundColour
	int foregroundColour
	char character
}