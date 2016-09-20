package net.clgd.ccemux

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.emulation.KeyTranslator
import net.clgd.ccemux.terminal.TerminalComponent
import org.eclipse.xtend.lib.annotations.Accessors

class EmulatorWindow extends JFrame implements KeyListener {
	static val EMU_WINDOW_TITLE = "CCEmuX" 
	
	@Accessors(PUBLIC_GETTER) EmulatedComputer computer
	TerminalComponent termComponent
	
	var lastBlink = false
	
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
		
		// Required for tab to work
		focusTraversalKeysEnabled = false
		
		addKeyListener(this)
		
		// Make sure the window's contents fit.
		pack
		
		// Centre the window.
		locationRelativeTo = null
		
		lastBlink = CCEmuX.get.globalCursorBlink
	}
	
	def void update(float dt) {
		computer.update(dt)
		
		var doRepaint = false
		
		if (computer.terminal.changed) {
			doRepaint = true
			computer.terminal.clearChanged
		}
		
		if (CCEmuX.get.globalCursorBlink != lastBlink) {
			doRepaint = true
		}
		
		lastBlink = CCEmuX.get.globalCursorBlink
		
		if (doRepaint) {
			termComponent.cursorChar = computer.cursorChar
			termComponent.repaint()
		}
	}

	
	private static def isPrintableChar(char c) {
		val block = Character.UnicodeBlock.of(c)
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED &&
				block != null && block != Character.UnicodeBlock.SPECIALS
	}
	
	override keyPressed(KeyEvent e) {
		val Object[] params = newArrayOfSize(1)
		
		params.set(0, KeyTranslator.translateToCC(e.keyCode))
		computer.computer.queueEvent("key", params)
		
		if (isPrintableChar(e.keyChar)) {
			params.set(0, e.keyChar.toString)
			computer.computer.queueEvent("char", params)
		}
	}
	
	override keyReleased(KeyEvent e) {
		val Object[] params = newArrayOfSize(1)
		params.set(0, KeyTranslator.translateToCC(e.keyCode))
		computer.computer.queueEvent("key_up", params)
	}
	
	override keyTyped(KeyEvent e) {}	
}