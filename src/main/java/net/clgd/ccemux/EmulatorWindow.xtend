package net.clgd.ccemux

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.terminal.TerminalComponent

class EmulatorWindow extends JFrame {
	static val EMU_WINDOW_TITLE = "CCEmuX" 
	
	EmulatedComputer computer
	
	new() {
		super(EMU_WINDOW_TITLE)
		
		layout = new BorderLayout
		minimumSize = new Dimension(300, 200)
		
		// Make sure the process ends when we close the window.
		defaultCloseOperation = EXIT_ON_CLOSE
		
		// TODO: Make these configurable
		val termWidth = 51
		val termHeight = 19
		
		val termPixelWidth = 18
		val termPixelHeight = 27
		
		computer = new EmulatedComputer(termWidth, termHeight)
		
		add(new TerminalComponent(
			computer.terminal,
			termPixelWidth, termPixelHeight
		), BorderLayout.CENTER)
		
		// Make sure the window's contents fit.
		pack
		
		// Centre the window.
		locationRelativeTo = null
	}
}