package net.clgd.ccemux.rendering.awt

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
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
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import net.clgd.ccemux.emulation.CCEmuX
import net.clgd.ccemux.emulation.CCEmuXConsts
import net.clgd.ccemux.emulation.EmulatedComputer
import net.clgd.ccemux.rendering.Renderer
import net.clgd.ccemux.Utils.isPrintableChar

class AWTRenderer(val emu: CCEmuX, val computer: EmulatedComputer) : Frame(), KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, Renderer {
	val EMU_WINDOW_TITLE = "CCEmuX"

	val termComponent = TerminalComponent(computer.terminal, emu.conf.getTermScale())

	val pixelWidth = 6 * emu.conf.getTermScale()
	val pixelHeight = 9 * emu.conf.getTermScale()

	var lastBlink = false
	var dragButton = 4

	var blinkLockedTime = 0.0f

	init {
		layout = BorderLayout()
		minimumSize = Dimension(300, 200)

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

		// properly stop emulator when window is closed
		addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent) = computer.dispose()
		})

		isResizable = false
		type = Window.Type.NORMAL

		// Make sure the window's contents fit.
		pack()

		// Centre the window.
		setLocationRelativeTo(null)

		lastBlink = CCEmuXConsts.getGlobalCursorBlink()
	}

	override fun onUpdate(dt: Float) {
		title = getWindowTitle()

		blinkLockedTime = Math.max(0.0f, blinkLockedTime - dt)
		termComponent.blinkLocked = blinkLockedTime > 0.0f

		if (isVisible) {
			var doRepaint = false

			if (computer.terminal.changed) {
				doRepaint = true
				computer.terminal.clearChanged()
			}

			if (CCEmuXConsts.getGlobalCursorBlink() != lastBlink) {
				doRepaint = true
			}

			lastBlink = CCEmuXConsts.getGlobalCursorBlink()

			if (doRepaint) {
				termComponent.cursorChar = computer.cursorChar
				termComponent.render(dt)
			}
		}
	}

	override fun onDispose() = dispose()

	fun mapPointToCC(p: Point): Point {
        val px = p.x - termComponent.margin
        val py = p.y - termComponent.margin

        val x = px / pixelWidth
        val y = py / pixelHeight

		return Point(x + 1, y + 1)
	}

	private fun handleCtrlPress(control: Char): Boolean {
		when (control) {
			't' -> computer.terminateProgram()
			'r' -> {
				if (!computer.isOn()) {
					computer.turnOn()
				} else {
					computer.reboot()
				}
			}
			's' -> computer.shutdown()
			'v' -> {
				val clipboard = Toolkit.getDefaultToolkit().systemClipboard
				val data = clipboard.getData(DataFlavor.stringFlavor) as String
				computer.pasteText(data)
			}
			else -> return false
		}

		return true
	}

	private fun getWindowTitle(): String {
		val label = computer.getLabel()

		if (label != null) {
			return EMU_WINDOW_TITLE + " - " + computer.getLabel() + " (Computer #" + computer.getID() + ")"
		} else {
			return EMU_WINDOW_TITLE + " - Computer #" + computer.getID()
		}
	}

	override fun onTerminalResized(width: Int, height: Int) {
		termComponent.resizeTerminal(width, height)
		pack()
	}

	override fun keyTyped(e: KeyEvent) {
		if (e.keyChar.isPrintableChar()) {
			computer.pressChar(e.keyChar)
			blinkLockedTime = 0.25f
		}
	}

	override fun keyPressed(e: KeyEvent) {
		computer.pressKey(KeyTranslator.translateToCC(e.keyCode), false)
		blinkLockedTime = 0.25f
	}

	override fun keyReleased(e: KeyEvent) {
		if (e.modifiers.and(Toolkit.getDefaultToolkit().menuShortcutKeyMask) != 0) {
			// For whatever stupid reason, Swing subtracts 96 from all chars when ctrl is held.
			val realChar: Char = e.keyChar + 96
			if (handleCtrlPress(realChar)) {
				return
			}
		}

		computer.pressKey(KeyTranslator.translateToCC(e.keyCode), true)
	}

	private fun fireMouseEvent(e: MouseEvent, press: Boolean) {
		val point = mapPointToCC(Point(e.x, e.y))
		computer.click(MouseTranslator.swingToCC(e.button), point.x, point.y, !press)
	}

	override fun mouseDragged(e: MouseEvent) {
		val point = mapPointToCC(Point(e.x, e.y))
		computer.drag(dragButton, point.x, point.y)
	}

	override fun mousePressed(e: MouseEvent) {
		fireMouseEvent(e, true)
		dragButton = MouseTranslator.swingToCC(e.button)
	}

	override fun mouseReleased(e: MouseEvent) {
		fireMouseEvent(e, false)
	}

	override fun mouseWheelMoved(e: MouseWheelEvent) {
		val amt = e.unitsToScroll
		val dir = if (amt > 0) 1 else -1
		val point = mapPointToCC(Point(e.x, e.y))
		computer.scroll(dir, point.x, point.y)
	}

	override fun mouseClicked(e: MouseEvent) {}

	override fun mouseEntered(e: MouseEvent) {}

	override fun mouseExited(e: MouseEvent) {}

	override fun mouseMoved(e: MouseEvent) {}
}
