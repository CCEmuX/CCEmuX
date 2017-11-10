package net.clgd.ccemux.rendering.awt;

import static net.clgd.ccemux.rendering.awt.KeyTranslator.translateToCC;
import static net.clgd.ccemux.rendering.awt.MouseTranslator.swingToCC;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import net.clgd.ccemux.rendering.TerminalFont;
import net.clgd.ccemux.rendering.TerminalFonts;
import org.apache.commons.io.IOUtils;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.emulation.*;
import net.clgd.ccemux.rendering.Renderer;

@Slf4j
public class AWTRenderer extends Renderer
		implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	public static final String EMU_WINDOW_TITLE = "CCEmuX";

	private static final double ACTION_TIME = 0.5;

	private static boolean isPrintableChar(char c) {
		UnicodeBlock block = UnicodeBlock.of(c);
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null
				&& block != UnicodeBlock.SPECIALS;
	}

	private final List<Renderer.Listener> listeners = new ArrayList<>();

	private final Frame frame;

	@Override
	public void addListener(Renderer.Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Renderer.Listener listener) {
		listeners.remove(listener);
	}

	private final EmulatedComputer computer;
	private final TerminalComponent termComponent;

	private final int pixelWidth;
	private final int pixelHeight;

	private boolean lastBlink = false;
	private int dragButton = 4;
	private Point lastDragSpot = null;

	private double blinkLockedTime = 0d;

	private boolean paletteChanged = false;

	private double terminateTimer = -1;
	private double shutdownTimer = -1;
	private double rebootTimer = -1;

	public AWTRenderer(EmulatedComputer computer, EmuConfig config) {
		frame = new Frame(EMU_WINDOW_TITLE);

		this.computer = computer;
		computer.terminal.getEmulatedPalette().addListener((i, r, g, b) -> paletteChanged = true);

		pixelWidth = (int) (6 * config.termScale.get());
		pixelHeight = (int) (9 * config.termScale.get());

		frame.setLayout(new BorderLayout());
		// setMinimumSize(new Dimension(300, 200));

		termComponent = new TerminalComponent(computer.terminal, config.termScale.get());
		frame.add(termComponent, BorderLayout.CENTER);

		// required for tab to work
		frame.setFocusTraversalKeysEnabled(false);
		termComponent.setFocusTraversalKeysEnabled(false);

		termComponent.addKeyListener(this);
		termComponent.addMouseListener(this);
		termComponent.addMouseMotionListener(this);
		termComponent.addMouseWheelListener(this);

		frame.addKeyListener(this);
		frame.addMouseListener(this);
		frame.addMouseMotionListener(this);
		frame.addMouseWheelListener(this);

		// properly stop emulator when window is closed
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				AWTRenderer.this.dispose();
				listeners.forEach(Renderer.Listener::onClosed);
			}
		});

		frame.setResizable(false);
		frame.setDropTarget(new DropTarget(null, new DropTargetListener() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					val flavors = dtde.getCurrentDataFlavors();
					if (Arrays.stream(flavors).anyMatch(DataFlavor::isFlavorJavaFileListType)) {
						log.debug("Accepting file drag and drop for computer #{}", computer.getID());
						dtde.acceptDrop(DnDConstants.ACTION_COPY);

						@SuppressWarnings("unchecked")
						val data = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
						computer.copyFiles(data, "/");
						JOptionPane.showMessageDialog(null, "Files have been copied to the computer root.",
								"Files copied", JOptionPane.INFORMATION_MESSAGE);
					} else if (DataFlavor.selectBestTextFlavor(flavors) != null) {
						val f = DataFlavor.selectBestTextFlavor(flavors);

						log.debug("Accepting text drag and drop for computer #{}", computer.getID());
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						val r = f.getReaderForText(dtde.getTransferable());
						computer.paste(IOUtils.toString(r));
					}
				} catch (Exception e) {
					log.error("Error processing drag and drop", e);
					JOptionPane.showMessageDialog(null, e, "Error processing file drop", JOptionPane.ERROR_MESSAGE);
				}
			}

			private void handleDragEvent(DropTargetDragEvent dtde) {
				val flavors = dtde.getCurrentDataFlavorsAsList();
				if (flavors.stream().anyMatch(f -> f.isFlavorJavaFileListType() || f.isFlavorTextType())) {
					dtde.acceptDrag(DnDConstants.ACTION_COPY);
				}
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
				handleDragEvent(dtde);
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
				handleDragEvent(dtde);
			}

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
				handleDragEvent(dtde);
			}

			@Override
			public void dragExit(DropTargetEvent dte) {}
		}));

		// fit to contents
		frame.pack();

		// center window in screen
		frame.setLocationRelativeTo(null);

		// set icon
		try {
			frame.setIconImage(ImageIO.read(AWTRenderer.class.getResourceAsStream("/img/icon.png")));
		} catch (IOException e) {
			log.warn("Failed to set taskbar icon", e);
		}

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
		frame.setTitle(getWindowTitle());
		blinkLockedTime = Math.max(0, blinkLockedTime - dt);
		termComponent.blinkLocked = blinkLockedTime > 0;

		if (isVisible()) {
			// Handle action keys
			if (shutdownTimer >= 0 && shutdownTimer < ACTION_TIME) {
				shutdownTimer += dt;
				if (shutdownTimer >= ACTION_TIME) computer.shutdown();
			}

			if (rebootTimer >= 0 && rebootTimer < ACTION_TIME) {
				rebootTimer += dt;
				if (rebootTimer >= ACTION_TIME) {
					if (computer.isOn()) {
						computer.reboot();
					} else {
						computer.turnOn();
					}
				}
			}

			if (terminateTimer >= 0 && terminateTimer < ACTION_TIME) {
				terminateTimer += dt;
				if (terminateTimer >= ACTION_TIME) computer.terminate();
			}

			boolean doRepaint = paletteChanged;

			if (computer.terminal.getChanged()) {
				doRepaint = true;
				computer.terminal.clearChanged();
			}

			if (CCEmuX.getGlobalCursorBlink() != lastBlink) {
				doRepaint = true;
			}

			lastBlink = CCEmuX.getGlobalCursorBlink();

			if (doRepaint) {
				// TODO
				// termComponent.cursorChar = computer.cursorChar;
				AWTTerminalFont font = (AWTTerminalFont) TerminalFonts.getFontsFor(getClass()).getBest(this);
				termComponent.render(font, dt);
			}
		}
	}

	@Override
	public TerminalFont loadFont(URL url) throws IOException {
		return new AWTTerminalFont(url);
	}

	private Point mapPointToCC(Point p) {
		int px = p.x - termComponent.margin;
		int py = p.y - termComponent.margin;

		int x = px / pixelWidth;
		int y = py / pixelHeight;

		return new Point(x + 1, y + 1);
	}

	/**
	 * Determine whether {@code key} and {@code char} events should be queued.
	 *
	 * If any of the action keys are pressed (terminate, shutdown, reboot) then such events will
	 * be blocked.
	 *
	 * @return Whether such events should be queued.
	 */
	private boolean allowKeyEvents() {
		return shutdownTimer < 0 && rebootTimer < 0 && terminateTimer < 0;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (isPrintableChar(e.getKeyChar()) & allowKeyEvents()) {
			computer.pressChar(e.getKeyChar());
			blinkLockedTime = 0.25d;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
			char real = (char) (e.getKeyChar() + 96);

			// Start action timers
			if (real == 's' && shutdownTimer < 0) shutdownTimer = 0;
			if (real == 'r' && rebootTimer < 0) rebootTimer = 0;
			if (real == 't' && terminateTimer < 0) terminateTimer = 0;

			// Handle pasting, this exists early.
			if (real == 'v') {
				try {
					computer.paste((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
				} catch (HeadlessException | UnsupportedFlavorException | IOException er) {
					log.error("Could not read clipboard", er);
				}
				return;
			}
		}

		if (allowKeyEvents()) {
			computer.pressKey(translateToCC(e.getKeyCode()), false);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
			char real = (char) (e.getKeyChar() + 96);

			// Reset control timers
			if (real == 's') shutdownTimer = -1;
			if (real == 'r') rebootTimer = -1;
			if (real == 't') terminateTimer = -1;
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
		if (p.equals(lastDragSpot)) return;

		computer.drag(dragButton, p.x, p.y);
		lastDragSpot = p;
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

	@Override
	public boolean isVisible() {
		return frame.isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		frame.setVisible(visible);
	}

	@Override
	public void dispose() {
		frame.dispose();
	}
}
