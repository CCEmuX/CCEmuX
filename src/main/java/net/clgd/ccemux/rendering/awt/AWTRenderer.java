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
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import com.google.common.io.CharStreams;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.clgd.ccemux.api.Utils;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.rendering.Renderer;
import net.clgd.ccemux.api.rendering.TerminalFont;
import net.clgd.ccemux.plugins.builtin.AWTPlugin.AWTConfig;

@Slf4j
public class AWTRenderer implements Renderer, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, WindowFocusListener {

	public static final String EMU_WINDOW_TITLE = "CCEmuX";

	private static final double ACTION_TIME = 0.5;

	@Getter(lazy = true)
	private static final AWTTerminalFont font = loadBestFont();

	private static AWTTerminalFont loadBestFont() {
		return TerminalFont.getBest(AWTTerminalFont::new);
	}

	private final List<Renderer.Listener> listeners = new ArrayList<>();

	private final Frame frame;

	@Override
	public void addListener(@Nonnull Renderer.Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(@Nonnull Renderer.Listener listener) {
		listeners.remove(listener);
	}

	private final EmulatedComputer computer;
	private final TerminalComponent termComponent;
	private final AWTConfig rendererConfig;

	private final int pixelWidth;
	private final int pixelHeight;

	private boolean lastBlink = false;
	private int lastDragButton = -1;
	private Point lastDragPosition = null;

	private double blinkLockedTime = 0d;

	private double terminateTimer = -1;
	private double shutdownTimer = -1;
	private double rebootTimer = -1;

	private final BitSet keysDown = new BitSet(256);

	public AWTRenderer(EmulatedComputer computer, EmuConfig config, AWTConfig rendererConfig) {
		frame = new Frame(EMU_WINDOW_TITLE);

		this.computer = computer;
		this.rendererConfig = rendererConfig;

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
		frame.addWindowFocusListener(this);

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
						try (Reader r = f.getReaderForText(dtde.getTransferable())) {
							computer.paste(CharStreams.toString(r));
						}
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

		lastBlink = Utils.getGlobalCursorBlink();
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

			boolean doRepaint = false;

			if (computer.terminal.getChanged()) {
				doRepaint = true;
				computer.terminal.clearChanged();
			}

			if (computer.terminal.getPalette().isChanged()) {
				doRepaint = true;
				computer.terminal.getPalette().setChanged(false);
			}

			if (Utils.getGlobalCursorBlink() != lastBlink) {
				doRepaint = true;
			}

			lastBlink = Utils.getGlobalCursorBlink();

			if (doRepaint) {
				// TODO
				// termComponent.cursorChar = computer.cursorChar;
				//AWTTerminalFont font = (AWTTerminalFont) TerminalFonts.getFontsFor(getClass()).getBest(this);
				termComponent.render(getFont(), dt);
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

	/**
	 * Determine whether {@code key} and {@code char} events should be queued.
	 *
	 * If any of the action keys are pressed (terminate, shutdown, reboot) then
	 * such events will be blocked.
	 *
	 * @return Whether such events should be queued.
	 */
	private boolean allowKeyEvents() {
		return shutdownTimer < 0 && rebootTimer < 0 && terminateTimer < 0;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (Utils.isPrintableChar(e.getKeyChar()) && allowKeyEvents()) {
			computer.pressChar(e.getKeyChar());
			blinkLockedTime = 0.25d;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// Pasting should be handled first as it blocks all events
		boolean hasModifier = (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
		if (rendererConfig.nativePaste.get()
			? DefaultEditorKit.pasteAction.equals(
			((InputMap) UIManager.getLookAndFeelDefaults().get("TextField.focusInputMap"))
				.get(KeyStroke.getKeyStrokeForEvent(e)))
			: hasModifier && e.getKeyCode() == KeyEvent.VK_V) {
			try {
				computer.paste((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
			} catch (HeadlessException | UnsupportedFlavorException | IOException er) {
				log.error("Could not read clipboard", er);
			}
			return;
		}

		if (allowKeyEvents()) {
			int code = translateToCC(e.getKeyCode(), e.getKeyLocation());
			if (code >= 0) {
				computer.pressKey(code, keysDown.get(code));
				keysDown.set(code);
			}
		}

		// Start action timers
		if (hasModifier) {
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_S && shutdownTimer < 0) shutdownTimer = 0;
			if (key == KeyEvent.VK_R && rebootTimer < 0) rebootTimer = 0;
			if (key == KeyEvent.VK_T && terminateTimer < 0) terminateTimer = 0;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Reset the action timers if either the letter or command keys are released.
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_S) shutdownTimer = -1;
		if (key == KeyEvent.VK_R) rebootTimer = -1;
		if (key == KeyEvent.VK_T) terminateTimer = -1;
		if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) == 0) {
			shutdownTimer = rebootTimer = terminateTimer = -1;
		}

		int code = translateToCC(key, e.getKeyLocation());
		if (code >= 0 && keysDown.get(code)) {
			keysDown.clear(code);
			computer.releaseKey(code);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point p = mapPointToCC(new Point(e.getX(), e.getY()));
		if (lastDragButton == -1 || p.equals(lastDragPosition)) return;

		computer.drag(lastDragButton, p.x, p.y);
		lastDragPosition = p;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Point p = mapPointToCC(new Point(e.getX(), e.getY()));
		int button = swingToCC(e.getButton());
		if (button == -1) return;

		computer.click(button, p.x, p.y, false);
		lastDragButton = button;
		lastDragPosition = p;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Point p = mapPointToCC(new Point(e.getX(), e.getY()));
		int button = swingToCC(e.getButton());
		if (button == -1 || button != lastDragButton) return;

		computer.click(button, p.x, p.y, true);
		lastDragButton = -1;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		e.consume();
		int amt = e.getUnitsToScroll();
		if (amt != 0) {
			int dir = amt > 0 ? 1 : -1;
			Point p = mapPointToCC(new Point(e.getX(), e.getY()));
			computer.scroll(dir, p.x, p.y);
		}
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

	@Override
	public void windowGainedFocus(WindowEvent e) {
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
		for (int i = keysDown.size(); i >= 0; i--) {
			if (keysDown.get(i)) {
				keysDown.clear(i);
				computer.releaseKey(i);
			}
		}

		rebootTimer = shutdownTimer = terminateTimer = -1;
	}
}
