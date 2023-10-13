package net.clgd.ccemux.rendering.javafx;

import static com.google.common.primitives.Ints.constrainToRange;
import static net.clgd.ccemux.api.rendering.TerminalFont.*;

import java.awt.Point;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dan200.computercraft.core.terminal.TextBuffer;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import net.clgd.ccemux.api.OperatingSystem;
import net.clgd.ccemux.api.Utils;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.rendering.PaletteAdapter;
import net.clgd.ccemux.plugins.builtin.JFXPlugin;

public class ComputerPane extends Pane implements EmulatedComputer.Listener {
	private static final Logger log = LoggerFactory.getLogger(ComputerPane.class);
	private static final boolean osx = OperatingSystem.get().equals(OperatingSystem.MacOSX);

	/**
	 * Time (in milliseconds) that a key combo must be held before triggering
	 */
	public static final long COMBO_TIME = 500;

	private final Canvas canvas;
	private final EmulatedComputer computer;
	private final JFXTerminalFont font;
	private final PaletteAdapter<Color> paletteAdapter;
	private final ReadOnlyDoubleProperty termScale;

	private final DoubleExpression margin;
	private final DoubleExpression charWidth;
	private final DoubleExpression charHeight;
	private final DoubleExpression totalWidth;
	private final DoubleExpression totalHeight;

	private boolean lastBlink = false;
	private double blinkLockedTime = 0;

	/**
	 * Map of currently-pressed key codes to the time (in millis) that they were
	 * first pressed
	 */
	private Map<KeyCode, Long> pressedKeys = new HashMap<>();

	/**
	 * The last CC X and Y coordinates of a mouse drag, to prevent duplicate events.
	 */
	private Point lastDragPosition;

	/**
	 * The button of the last drag/press event.
	 */
	private int lastDragButton;

	/**
	 * @return Whether the cursor should be shown
	 */
	private boolean cursorBlink() {
		return computer.terminal.getCursorBlink() && (Utils.getGlobalCursorBlink() || blinkLockedTime > 0);
	}

	public ComputerPane(EmulatedComputer computer, JFXTerminalFont font, ReadOnlyDoubleProperty termScale) {
		this.computer = computer;
		this.font = font;
		this.paletteAdapter = new PaletteAdapter<>(computer.terminal.getPalette(), Color::color);
		this.termScale = termScale;

		this.margin = termScale.multiply(BASE_MARGIN);
		this.charWidth = termScale.multiply(BASE_CHAR_WIDTH);
		this.charHeight = termScale.multiply(BASE_CHAR_HEIGHT);
		this.totalWidth = margin.multiply(2).add(charWidth.multiply(computer.terminal.getWidth()));
		this.totalHeight = margin.multiply(2).add(charHeight.multiply(computer.terminal.getHeight()));

		this.prefWidthProperty().bind(totalWidth);
		this.prefHeightProperty().bind(totalHeight);

		this.canvas = new Canvas(totalWidth.get(), totalHeight.get());
		canvas.widthProperty().bind(this.widthProperty());
		canvas.heightProperty().bind(this.heightProperty());

		canvas.widthProperty().addListener(o -> this.redraw());
		canvas.heightProperty().addListener(o -> this.redraw());

		// setup event listeners
		setOnKeyPressed(this::keyPressed);
		setOnKeyReleased(this::keyReleased);
		setOnKeyTyped(this::keyTyped);

		setOnMousePressed(this::mousePressed);
		setOnMouseReleased(this::mouseReleased);
		setOnMouseDragged(this::mouseDragged);

		setOnScroll(this::mouseScroll);

		setOnDragOver(this::dragOver);
		setOnDragDropped(e -> transferContents(e.getDragboard()));

		focusedProperty().addListener(this::focusChanged);

		this.setFocusTraversable(false);
		canvas.setFocusTraversable(false);

		this.getChildren().add(canvas);

		computer.addListener(this);
	}

	@Override
	public boolean isResizable() {
		// makes sure that this element is drawn with a rigid size and not
		// resized by parents
		return false;
	}

	private void redraw() {
		if (!Platform.isFxApplicationThread()) {
			// only draw on the JavaFX thread
			Platform.runLater(this::redraw);
			return;
		}

		synchronized (computer.terminal) {
			GraphicsContext g = canvas.getGraphicsContext2D();

			// cache some important values as primitives
			double fontScale = termScale.get();
			if (JFXPlugin.doubleFontScale.get()) fontScale *= 2;

			double m = margin.get();
			double cw = charWidth.get(), ch = charHeight.get();
			int tw = computer.terminal.getWidth(), th = computer.terminal.getHeight();

			// current position offsets
			double ox = 0, oy = 0;

			// height/width of current position
			double height, width;

			TextBuffer bg, fg, text;

			Image charImg;

			for (int y = 0; y < th; y++) {
				height = ch + ((y == 0 || y == th - 1) ? m : 0);

				bg = computer.terminal.getBackgroundColourLine(y);
				fg = computer.terminal.getTextColourLine(y);
				text = computer.terminal.getLine(y);

				for (int x = 0; x < tw; x++) {
					width = cw + ((x == 0 || x == tw - 1) ? m : 0);

					// draw background
					g.setFill(paletteAdapter.getColor(bg.charAt(x), PaletteAdapter.DEFAULT_BACKGROUND));
					g.fillRect(ox, oy, width, height);

					// draw character
					charImg = font.getCharImage(text.charAt(x), paletteAdapter.getColor(fg.charAt(x), PaletteAdapter.DEFAULT_FOREGROUND), fontScale);
					g.drawImage(charImg, ox + (x == 0 ? m : 0), oy + (y == 0 ? m : 0), cw, ch);

					ox += width;
				}

				ox = 0;
				oy += height;
			}

			// draw cursor
			if (cursorBlink()) {
				g.drawImage(
					font.getCharImage('_', paletteAdapter.getColor(computer.terminal.getTextColour(), PaletteAdapter.DEFAULT_FOREGROUND), fontScale),
					m + (cw * computer.terminal.getCursorX()), m + (ch * computer.terminal.getCursorY()), cw, ch);
			}

			lastBlink = cursorBlink();
		}
	}

	@Override()
	public void onAdvance(double dt) {
		blinkLockedTime = Math.max(0, blinkLockedTime - dt);

		boolean repaint = lastBlink != cursorBlink();
		repaint |= computer.terminal.getAndClearChanged();

		if (computer.terminal.getPalette().isChanged()) {
			repaint = true;
			computer.terminal.getPalette().setChanged(false);
		}

		if (repaint) {
			this.redraw();
		}
	}

	/**
	 * @return Whether one of the standard control-combos is in progress
	 */
	private boolean isComboInProgress() {
		return pressedKeys.containsKey(KeyCode.CONTROL) && (pressedKeys.containsKey(KeyCode.T)
			|| pressedKeys.containsKey(KeyCode.R) || pressedKeys.containsKey(KeyCode.S));
	}

	private void keyTyped(KeyEvent e) {
		// don't send char if pasting text
		if (e.isShortcutDown() && e.getCharacter().toLowerCase().trim().equals("v")) return;

		if (e.getCharacter().length() <= 0) return;
		char c = e.getCharacter().charAt(0);
		if (Utils.isPrintableChar(c)) computer.pressChar(c);
	}

	private void keyPressed(KeyEvent e) {
		int ccCode = JFXKeyTranslator.translateToCC(e.getCode());
		if (ccCode == 0) return;

		if (pressedKeys.containsKey(e.getCode())) {

			if (isComboInProgress()) {
				// check if combo is complete
				long m = System.currentTimeMillis();

				if (m - pressedKeys.get(KeyCode.CONTROL) >= COMBO_TIME) {
					// handle combo action
					if (m - pressedKeys.getOrDefault(KeyCode.T, m) >= COMBO_TIME) {
						computer.terminate();
					} else if (m - pressedKeys.getOrDefault(KeyCode.R, m) >= COMBO_TIME) {
						if (computer.isOn()) {
							computer.reboot();
						} else {
							computer.turnOn();
						}
					} else if (m - pressedKeys.getOrDefault(KeyCode.S, m) >= COMBO_TIME) {
						computer.shutdown();
					}

					// prevent the combo from triggering again for a while
					pressedKeys.replace(KeyCode.CONTROL, m);
				}
			} else {
				computer.pressKey(ccCode, true);
			}
		} else {
			pressedKeys.put(e.getCode(), System.currentTimeMillis());

			// don't send key if pasting text
			if (e.getCode().equals(KeyCode.V) && e.isShortcutDown()) return;

			computer.pressKey(ccCode, false);
		}

		blinkLockedTime = 0.25;
	}

	private void keyReleased(KeyEvent e) {
		int ccCode = JFXKeyTranslator.translateToCC(e.getCode());
		if (ccCode == 0) return;

		if (e.isShortcutDown() && osx) {
			if (!pressedKeys.containsKey(e.getCode())) {
				// OSX is haunted, and when cmd is held, sends keyReleased
				// events when it should send keyPressed events
				keyPressed(e);
				return;
			}
		}

		pressedKeys.remove(e.getCode());

		if (e.getCode().equals(KeyCode.V) && e.isShortcutDown()) {
			transferContents(Clipboard.getSystemClipboard());
		} else {
			computer.releaseKey(ccCode);
		}
	}

	private Point coordsToCC(double x, double y) {
		return new Point(
			1 + constrainToRange((int) Math.floor((x - margin.get()) / charWidth.get()), 0,
				computer.terminal.getWidth()),
			1 + constrainToRange((int) Math.floor((y - margin.get()) / charHeight.get()), 0,
				computer.terminal.getHeight())
		);
	}

	private void mousePressed(MouseEvent e) {
		Point p = coordsToCC(e.getX(), e.getY());
		int button = JFXMouseTranslator.toCC(e.getButton());
		if (button == -1) return;

		computer.click(button, p.x, p.y, false);
		lastDragButton = button;
		lastDragPosition = p;
	}

	private void mouseReleased(MouseEvent e) {
		Point p = coordsToCC(e.getX(), e.getY());
		int button = JFXMouseTranslator.toCC(e.getButton());
		if (button == -1 || button != lastDragButton) return;

		computer.click(button, p.x, p.y, true);
		lastDragButton = -1;
	}

	private void mouseDragged(MouseEvent e) {
		Point p = coordsToCC(e.getX(), e.getY());
		if (lastDragButton == -1 || p.equals(lastDragPosition)) return;

		computer.drag(lastDragButton, p.x, p.y);
		lastDragPosition = p;
	}

	private void mouseScroll(ScrollEvent e) {
		Point p = coordsToCC(e.getX(), e.getY());
		computer.scroll(-1 * (int) (e.getDeltaY() / e.getMultiplierY()), p.x, p.y);
	}

	private void focusChanged(Observable e) {
		if (!focusedProperty().get()) {
			for (KeyCode code : pressedKeys.keySet()) {
				int ccCode = JFXKeyTranslator.translateToCC(code);
				if (ccCode != 0) computer.releaseKey(ccCode);
			}
			pressedKeys.clear();
		}
	}

	private void dragOver(DragEvent e) {
		if (e.getDragboard().hasFiles() || e.getDragboard().hasString()) {
			e.acceptTransferModes(TransferMode.COPY);
		}
	}

	public boolean transferContents(Clipboard cb) {
		if (cb.hasFiles()) {
			try {
				computer.transferFiles(cb.getFiles());
				return true;
			} catch (IOException e1) {
				log.error("Error copying files {}", cb.getFiles(), e1);

				Alert a = new Alert(AlertType.ERROR);
				a.setTitle("File copy error");
				a.setHeaderText("File copy error");
				a.setContentText("There was an error copying file to computer ID " + computer.getID() + ":\n"
					+ e1.getLocalizedMessage() + "\n\nSee logs for more information");
				a.initStyle(StageStyle.UTILITY);
				a.show();
				return false;
			}
		} else if (cb.hasString()) {
			computer.paste(cb.getString());
			return true;
		} else {
			return false;
		}
	}
}
