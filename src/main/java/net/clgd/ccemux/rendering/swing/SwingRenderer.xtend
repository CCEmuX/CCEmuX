package net.clgd.ccemux.rendering.swing

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit
import java.awt.Window
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import javax.swing.JFrame
import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.emulation.KeyTranslator
import net.clgd.ccemux.emulation.MouseTranslator
import org.eclipse.xtend.lib.annotations.Accessors
import net.clgd.ccemux.rendering.Renderer

class SwingRenderer extends JFrame implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, EmulatedComputer.Listener, Renderer {
	static val EMU_WINDOW_TITLE = "CCEmuX"

	@Accessors(PUBLIC_GETTER) EmulatedComputer computer
	TerminalComponent termComponent

	final int pixelWidth
	final int pixelHeight

	var lastBlink = false
	var dragButton = 4

	var blinkLockedTime = 0.0f

	new(CCEmuX emu, EmulatedComputer computer) {
		super(EMU_WINDOW_TITLE)

		pixelWidth = 6 * emu.conf.termScale
		pixelHeight = 9 * emu.conf.termScale

		layout = new BorderLayout
		minimumSize = new Dimension(300, 200)

		// Make sure the process ends when we close the window.
		defaultCloseOperation = EXIT_ON_CLOSE

		this.computer = computer
		computer.addListener(this)

		termComponent = new TerminalComponent(
			computer.terminal,
			pixelWidth,
			pixelHeight
		)

		add(termComponent, BorderLayout.CENTER)

		// Required for tab to work
		termComponent.focusTraversalKeysEnabled = false

		termComponent.addKeyListener(this)
		termComponent.addMouseListener(this)
		termComponent.addMouseMotionListener(this)
		termComponent.addMouseWheelListener(this)

		addKeyListener(this)
		addMouseListener(this)
		addMouseMotionListener(this)
		addMouseWheelListener(this)

		resizable = false
		type = Window.Type.NORMAL

		// Make sure the window's contents fit.
		pack

		// Center the window.
		locationRelativeTo = null

		lastBlink = CCEmuX.globalCursorBlink
	}

	override void update(float dt) {
		blinkLockedTime = Math.max(0.0f, blinkLockedTime - dt)
		termComponent.blinkLocked = blinkLockedTime > 0.0f

		// computer.update(dt)
		var doRepaint = false

		if (computer.terminal.changed) {
			doRepaint = true
			computer.terminal.clearChanged
		}

		if (CCEmuX.globalCursorBlink != lastBlink) {
			doRepaint = true
		}

		lastBlink = CCEmuX.globalCursorBlink

		if (doRepaint) {
			termComponent.cursorChar = computer.cursorChar
			termComponent.render(dt)
		}
	}

	private def mapPointToCC(Point p) {
		return new Point(
			p.x / pixelWidth + 1,
			p.y / pixelHeight + 1
		)
	}

	private static def isPrintableChar(char c) {
		val block = Character.UnicodeBlock.of(c)
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null &&
			block != Character.UnicodeBlock.SPECIALS
	}

	private def handleCtrlPress(char control) {
		if (control == 't'.charAt(0)) {
			computer.terminateProgram
		} else if (control == 'r'.charAt(0)) {
			if (!computer.on) {
				computer.turnOn
			} else {
				computer.reboot
			}
		} else if (control == 's'.charAt(0)) {
			computer.shutdown
		} else if (control == 'v'.charAt(0)) {
			val clipboard = Toolkit.defaultToolkit.systemClipboard
			val data = clipboard.getData(DataFlavor.stringFlavor) as String
			computer.pasteText(data)
		} else {
			return false
		}

		return true
	}

	override keyTyped(KeyEvent e) {
		if (isPrintableChar(e.keyChar)) {
			computer.pressChar(e.keyChar)
			blinkLockedTime = 0.25f
		}
	}

	override keyPressed(KeyEvent e) {
		computer.pressKey(KeyTranslator.translateToCC(e.keyCode), false)
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

		computer.pressKey(KeyTranslator.translateToCC(e.keyCode), true)
	}

	private def fireMouseEvent(MouseEvent e, boolean press) {
		val point = mapPointToCC(new Point(e.x, e.y))
		computer.click(MouseTranslator.swingToCC(e.button), point.x, point.y, !press)
	}

	override mouseDragged(MouseEvent e) {
		val point = mapPointToCC(new Point(e.x, e.y))
		computer.drag(dragButton, point.x, point.y)
	}

	override mousePressed(MouseEvent e) {
		fireMouseEvent(e, true)
		dragButton = MouseTranslator.swingToCC(e.button)
	}

	override mouseReleased(MouseEvent e) {
		fireMouseEvent(e, false)
	}

	override mouseWheelMoved(MouseWheelEvent e) {
		val amt = e.unitsToScroll
		val dir = if(amt > 0) 1 else -1
		val point = mapPointToCC(new Point(e.x, e.y))
		computer.scroll(dir, point.x, point.y)
	}

	override mouseClicked(MouseEvent e) {}

	override mouseEntered(MouseEvent e) {}

	override mouseExited(MouseEvent e) {}

	override mouseMoved(MouseEvent e) {}
}
