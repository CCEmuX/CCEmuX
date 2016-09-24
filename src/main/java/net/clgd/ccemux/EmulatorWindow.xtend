package net.clgd.ccemux

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import javax.swing.JFrame
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.emulation.KeyTranslator
import net.clgd.ccemux.terminal.TerminalComponent
import org.eclipse.xtend.lib.annotations.Accessors

class EmulatorWindow extends JFrame implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	static val EMU_WINDOW_TITLE = "CCEmuX" 
	
	@Accessors(PUBLIC_GETTER) EmulatedComputer computer
	TerminalComponent termComponent
	
	val pixelWidth = 6 * CCEmuX.get.conf.termScale
	val pixelHeight = 9 * CCEmuX.get.conf.termScale
	
	var lastBlink = false
	var dragButton = 4
	
	var blinkLockedTime = 0.0f
	
	new() {
		super(EMU_WINDOW_TITLE)
		
		layout = new BorderLayout
		minimumSize = new Dimension(300, 200)
		
		// Make sure the process ends when we close the window.
		defaultCloseOperation = EXIT_ON_CLOSE
		
		val termWidth = CCEmuX.get.conf.termWidth
		val termHeight = CCEmuX.get.conf.termHeight
		
		computer = new EmulatedComputer(termWidth, termHeight)
		
		termComponent = new TerminalComponent(
			computer.terminal,
			pixelWidth, pixelHeight
		)
		
		add(termComponent, BorderLayout.CENTER)
		
		// Required for tab to work
		termComponent.focusTraversalKeysEnabled = false
		
		termComponent.addKeyListener(this)
		termComponent.addMouseListener(this)
		termComponent.addMouseMotionListener(this)
		termComponent.addMouseWheelListener(this)
		
		// Make sure the window's contents fit.
		pack
		
		// Centre the window.
		locationRelativeTo = null
		
		lastBlink = CCEmuX.get.globalCursorBlink
	}
	
	def void update(float dt) {
		blinkLockedTime = Math.max(0.0f, blinkLockedTime - dt)
		termComponent.blinkLocked = blinkLockedTime > 0.0f
		
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
			termComponent.render(dt)
		}
	}
	
	private def mapPointToCC(Point p) {
		return new Point(
			p.x / pixelWidth,
			p.y / pixelHeight
		)
	}
	
	private static def mouseButtonToCC(int button) {
		switch (button) {
			case MouseEvent.BUTTON1: // Left button
				return 1
				
			case MouseEvent.BUTTON2: // Middle button
				return 3
				
			case MouseEvent.BUTTON3: // Right button
				return 2
		}
		
		CCEmuX.get.logger.info("Got gay button: " + button)
		return 4
	}
	
	private static def isPrintableChar(char c) {
		val block = Character.UnicodeBlock.of(c)
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED &&
				block != null && block != Character.UnicodeBlock.SPECIALS
	}
	
	private def handleCtrlPress(char control) {
		if (control == 't'.charAt(0)) {
			computer.computer.queueEvent("terminate", newArrayList())
		} else if (control == 'r'.charAt(0)) {
			if (!computer.computer.on) {
				computer.computer.turnOn
			} else {
				computer.computer.reboot
			}
		} else if (control == 's'.charAt(0)) {
			computer.computer.shutdown
		} else {
			return false
		}
			
		return true
	}
	
	override keyTyped(KeyEvent e) {
		if (isPrintableChar(e.keyChar)) {
			computer.computer.queueEvent("char", newArrayList(e.keyChar.toString))
			blinkLockedTime = 0.25f
		}
	}
	
	override keyPressed(KeyEvent e) {
		computer.computer.queueEvent("key", newArrayList(KeyTranslator.translateToCC(e.keyCode)))
		blinkLockedTime = 0.25f
	}
	
	override keyReleased(KeyEvent e) {
		if (e.modifiers.bitwiseAnd(Toolkit.defaultToolkit.menuShortcutKeyMask) != 0) {
			// For whatever stupid reason, Swing subtracts 96 from all chars when ctrl is held.
			val realChar = (e.keyChar + 96) as char
			if (handleCtrlPress(realChar)) {
				return
			}
		}
		
		computer.computer.queueEvent("key_up", newArrayList(KeyTranslator.translateToCC(e.keyCode)))
	}
	
	private def fireMouseEvent(MouseEvent e, boolean press) {
		val point = mapPointToCC(new Point(e.x, e.y))
		
		computer.computer.queueEvent(
			if (press) "mouse_click" else "mouse_up",
			newArrayList(
				mouseButtonToCC(e.button), point.x + 1, point.y + 1
			)
		)
	}
	
	override mouseDragged(MouseEvent e) {
		val point = mapPointToCC(new Point(e.x, e.y))
		computer.computer.queueEvent(
			"mouse_drag",
			newArrayList(dragButton, point.x + 1, point.y + 1)
		)
	}
	
	override mousePressed(MouseEvent e) {
		fireMouseEvent(e, true)
		dragButton = mouseButtonToCC(e.button)
	}
	
	override mouseReleased(MouseEvent e) {
		fireMouseEvent(e, false)
	}
	
	override mouseWheelMoved(MouseWheelEvent e) {
		val amt = e.unitsToScroll
		val dir = if (amt > 0) 1 else -1
		val point = mapPointToCC(new Point(e.x, e.y))
		
		computer.computer.queueEvent(
			"mouse_scroll",
			newArrayList(dir, point.x + 1, point.y + 1)
		)	
	}
	
	override mouseClicked(MouseEvent e) {}
	
	override mouseEntered(MouseEvent e) {}
	
	override mouseExited(MouseEvent e) {}
	
	override mouseMoved(MouseEvent e) {}	
}