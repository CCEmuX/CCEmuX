package net.clgd.ccemux.rendering.awt;

import static net.clgd.ccemux.rendering.awt.KeyTranslator.translateToCC;
import static net.clgd.ccemux.rendering.awt.MouseTranslator.swingToCC;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.core.util.IOUtils;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.RendererConfig;

@Slf4j
public class AWTRenderer extends Frame
		implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, Renderer {

	private static final long serialVersionUID = 374030924274589331L;

	public static final String EMU_WINDOW_TITLE = "CCEmuX";

	private static boolean isPrintableChar(char c) {
		UnicodeBlock block = UnicodeBlock.of(c);
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null
				&& block != UnicodeBlock.SPECIALS;
	}

	private final List<Renderer.Listener> listeners = new ArrayList<>();

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

	public AWTRenderer(EmulatedComputer computer, RendererConfig config) {
		super(EMU_WINDOW_TITLE);

		this.computer = computer;

		pixelWidth = (int) (6 * config.termScale);
		pixelHeight = (int) (9 * config.termScale);

		setLayout(new BorderLayout());
		// setMinimumSize(new Dimension(300, 200));

		termComponent = new TerminalComponent(computer.terminal, config.termScale);
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
				AWTRenderer.this.dispose();
				listeners.forEach(Renderer.Listener::onClosed);
			}
		});

		setResizable(false);
		setDropTarget(new DropTarget(null, new DropTargetListener() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					val flavors = dtde.getCurrentDataFlavors();
					if (Arrays.stream(flavors).anyMatch(f -> f.isFlavorJavaFileListType())) {
						log.debug("Accepting file drag and drop for computer #{}", computer.getID());
						dtde.acceptDrop(DnDConstants.ACTION_COPY);

						@SuppressWarnings("unchecked")
						val data = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
						computer.copyFiles(data, "/");
					} else if (DataFlavor.selectBestTextFlavor(flavors) != null) {
						val f = DataFlavor.selectBestTextFlavor(flavors);

						log.debug("Accepting text drag and drop for computer ${}", computer.getID());
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
		pack();

		// center window in screen
		setLocationRelativeTo(null);

		// set icon
		try {
			setIconImage(ImageIO.read(AWTRenderer.class.getResourceAsStream("/icon.png")));
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
				// TODO
				// termComponent.cursorChar = computer.cursorChar;
				termComponent.render(dt);
			}
		}
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
				computer.paste(
						(String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
			} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
				log.error("Could not read clipboard", e);
			}
		} else {
			return false;
		}

		return true;
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
			// For whatever stupid reason, Swing subtracts 96 from all chars
			// when ctrl is held.
			char real = (char) (e.getKeyChar() + 96);
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
}
