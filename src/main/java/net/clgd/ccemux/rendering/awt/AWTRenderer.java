package net.clgd.ccemux.rendering.awt;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;

import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.Renderer;

import static net.clgd.ccemux.rendering.awt.KeyTranslator.*;
import static net.clgd.ccemux.rendering.awt.MouseTranslator.*;

public class AWTRenderer extends Frame
		implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, Renderer {
	public static final String EMU_WINDOW_TITLE = "CCEmuX";

	private static boolean isPrintableChar(char c) {
		UnicodeBlock block = UnicodeBlock.of(c);
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null && block != UnicodeBlock.SPECIALS;
	}
	
	public final EmulatedComputer computer;
	public final TerminalComponent termComponent;

	public final int pixelWidth;
	public final int pixelHeight;

	public boolean lastBlink = false;
	public int dragButton = 4;

	public double blinkLockedTime = 0d;

	public AWTRenderer(EmulatedComputer computer) {
		super(EMU_WINDOW_TITLE);

		this.computer = computer;

		pixelWidth = 6 * computer.emu.conf.getTermScale();
		pixelHeight = 9 * computer.emu.conf.getTermScale();

		setLayout(new BorderLayout());
		// setMinimumSize(new Dimension(300, 200));

		termComponent = new TerminalComponent(computer.terminal, computer.emu.conf.getTermScale());
		add(termComponent, BorderLayout.CENTER);
		
		// required for tab to work
		termComponent.setFocusTraversalKeysEnabled(false);
		
		termComponent.addKeyListener(this);
		termComponent.addMouseListener(this);
		termComponent.addMouseMotionListener(this);
		termComponent.addMouseWheelListener(this);
		
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		
		// properly stop emulator when window is closed
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				computer.dispose();
			}
		});
		
		setResizable(false);
		
		// fit to contents
		pack();
		
		// center window in screen
		setLocationRelativeTo(null);
		
		lastBlink = CCEmuX.getGlobalCursorBlink();
	}

	private String getWindowTitle() {
		int id = computer.getID();
		String title = EMU_WINDOW_TITLE + " - ";
		
		if (computer.getLabel() != null) {
			title += computer.getLabel() + " (Computer #" + id + ")";
		} else {
			title += "Computer #" + id;
		}
		
		return title;
	}
	
	@Override
	public void onAdvance(double dt) {
		setTitle(getWindowTitle());
		blinkLockedTime = Math.max(0, blinkLockedTime - dt);
		termComponent.blinkLocked = blinkLockedTime > 0;
		
		if (isVisible()) {
			boolean doRepaint = false;
			
			if (computer.terminal.getChanged()) {
				doRepaint = true;
				computer.terminal.clearChanged();
			}
			
			if (CCEmuX.getGlobalCursorBlink() != lastBlink) {
				doRepaint = true;
			}
			
			lastBlink = CCEmuX.getGlobalCursorBlink();
			
			if (doRepaint) {
				termComponent.cursorChar = computer.cursorChar;
				termComponent.render(dt);
			}
		}
	}

	@Override
	public void onDispose() {
		dispose();
	}

	private Point mapPointToCC(Point p) {
		int px = p.x - termComponent.margin;
		int py = p.y - termComponent.margin;
		
		int x = px / pixelWidth;
		int y = py / pixelHeight;
		
		return new Point(x + 1, y + 1);
	}
	
	private boolean handleCtrlPress(char control) {
		if (control == 't') {
			computer.terminate();
		} else if (control == 'r') {
			if (!computer.isOn()) {
				computer.turnOn();
			} else {
				computer.reboot();
			}
		} else if (control == 's') {
			computer.shutdown();
		} else if (control == 'v') {
			try {
				computer.paste((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
			} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
				computer.emu.logger.error("Could not read clipboard", e);
			}
		} else {
			return false;
		}
		
		return true;
	}
	
	@Override
	public void onTerminalResized(int width, int height) {
		termComponent.resizeTerminal(width, height);
		
		pack();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (isPrintableChar(e.getKeyChar())) {
			computer.pressChar(e.getKeyChar());
			blinkLockedTime = 0.25d;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		computer.pressKey(translateToCC(e.getKeyCode()), false);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
			// For whatever stupid reason, Swing subtracts 96 from all chars when ctrl is held.
			char real = (char)(e.getKeyChar() + 96);
			if (handleCtrlPress(real)) return;
		}
		
		computer.pressKey(translateToCC(e.getKeyCode()), true);
	}

	private void fireMouseEvent(MouseEvent e, boolean press) {
		Point p = mapPointToCC(new Point(e.getX(), e.getY()));
		computer.click(swingToCC(e.getButton()), p.x, p.y, !press);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point p = mapPointToCC(new Point(e.getX(), e.getY()));
		computer.drag(dragButton, p.x, p.y);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		fireMouseEvent(e, true);
		dragButton = swingToCC(e.getButton());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		fireMouseEvent(e, false);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int amt = e.getUnitsToScroll();
		int dir = amt > 0 ? 1 : -1;
		Point p = mapPointToCC(new Point(e.getX(), e.getY()));
		computer.scroll(dir, p.x, p.y);
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}
