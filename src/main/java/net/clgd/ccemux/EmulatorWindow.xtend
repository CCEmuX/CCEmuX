package net.clgd.ccemux

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame

class EmulatorWindow extends JFrame {
	static final String EMU_WINDOW_TITLE = "CCEmuX" 
	
	new() {
		super(EMU_WINDOW_TITLE);
		
		layout = new BorderLayout()
		
		// TODO: Temporary magic numbers. Change this.
		size = new Dimension(918, 513)
		preferredSize = new Dimension(918, 513)
		minimumSize = new Dimension(300, 200)
		
		// Make sure the process ends when we close the window.
		defaultCloseOperation = EXIT_ON_CLOSE
		
		// Centre the window.
		locationRelativeTo = null
	}
}