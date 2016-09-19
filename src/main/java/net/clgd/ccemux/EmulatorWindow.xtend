package net.clgd.ccemux

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.terminal.TerminalComponent
import org.eclipse.xtend.lib.annotations.Accessors

class EmulatorWindow extends JFrame {
	static val EMU_WINDOW_TITLE = "CCEmuX" 
	
	@Accessors(PUBLIC_GETTER) EmulatedComputer computer
	TerminalComponent termComponent
	
	new() {
		super(EMU_WINDOW_TITLE)
		
		layout = new BorderLayout
		minimumSize = new Dimension(300, 200)
		
		// Make sure the process ends when we close the window.
		defaultCloseOperation = EXIT_ON_CLOSE
		
		val termWidth = CCEmuX.get.conf.termWidth
		val termHeight = CCEmuX.get.conf.termHeight
		
		val termPixelWidth = 6 * CCEmuX.get.conf.termScale
		val termPixelHeight = 9 * CCEmuX.get.conf.termScale
		
		computer = new EmulatedComputer(termWidth, termHeight)
		
		termComponent = new TerminalComponent(
			computer.terminal,
			termPixelWidth, termPixelHeight
		)
		
		add(termComponent, BorderLayout.CENTER)
		
		// Make sure the window's contents fit.
		pack
		
		// Centre the window.
		locationRelativeTo = null
	}
	
	def void update(float dt) {
		computer.update(dt)
		
		if (computer.terminal.changed) {
			termComponent.repaint()
			computer.terminal.clearChanged
		}
	}
}