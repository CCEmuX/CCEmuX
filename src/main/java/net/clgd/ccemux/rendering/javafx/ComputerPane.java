package net.clgd.ccemux.rendering.javafx;

import static com.google.common.primitives.Ints.constrainToRange;
import static net.clgd.ccemux.rendering.TerminalFont.*;

import java.io.IOException;

import dan200.computercraft.core.terminal.TextBuffer;
import javafx.application.Platform;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.PaletteAdapter;

@Slf4j
public class ComputerPane extends StackPane implements EmulatedComputer.Listener {
	/**
	 * Time (in milliseconds) that a key combo must be held before triggering
	 */
	public static final long COMBO_TIME = 500;

	private final Canvas canvas;
	private final KeyboardCapturer kbCap;

	@Getter
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
	 * int[2] containing the last CC X and Y coordinates of a mouse drag, to
	 * prevent duplicate events
	 */
	private int[] lastDrag;

	/**
	 * @return Whether the cursor should be shown
	 */
	private boolean cursorBlink() {
		return computer.terminal.getCursorBlink() && (CCEmuX.getGlobalCursorBlink() || blinkLockedTime > 0);
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

		// handle mouse events
		setOnMousePressed(this::mousePressed);
		setOnMouseReleased(this::mouseReleased);
		setOnMouseDragged(this::mouseDragged);
		setOnDragOver(this::dragOver);
		setOnDragDropped(e -> transferContents(e.getDragboard()));

		setFocusTraversable(false);
		canvas.setFocusTraversable(false);

		this.kbCap = new KeyboardCapturer(this);

		// make sure that the text capture field keeps focus
		// @formatter:off
		kbCap.focusedProperty().addListener((s, o, n) -> { if (!n) requestFocus(); });
		// @formatter:on

		// ordering is important:
		// canvas must be the last item so that it's drawn over the keyboard
		// capture field
		getChildren().add(kbCap);
		getChildren().add(canvas);

		computer.addListener(this);
	}

	@Override
	public void requestFocus() {
		// the keyboard capturer should be focused, not the window
		kbCap.requestFocus();
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
			val g = canvas.getGraphicsContext2D();

			// cache some important values as primitives
			double s = termScale.get();
			double m = margin.get();
			double cw = charWidth.get(), ch = charHeight.get();
			int tw = computer.terminal.getWidth(), th = computer.terminal.getHeight();

			// current position offsets
			double ox = 0, oy = 0;

			// height/width of current position
			double height, width;

			TextBuffer bg, fg, text;

			for (int y = 0; y < th; y++) {
				height = ch + ((y == 0 || y == th - 1) ? m : 0);

				bg = computer.terminal.getBackgroundColourLine(y);
				fg = computer.terminal.getTextColourLine(y);
				text = computer.terminal.getLine(y);

				for (int x = 0; x < tw; x++) {
					width = cw + ((x == 0 || x == tw - 1) ? m : 0);

					// draw background
					g.setFill(paletteAdapter.getColor(bg.charAt(x)));
					g.fillRect(ox, oy, width, height);

					// draw character
					g.drawImage(font.getCharImage(text.charAt(x), paletteAdapter.getColor(fg.charAt(x)), s),
							ox + (x == 0 ? m : 0), oy + (y == 0 ? m : 0));

					ox += width;
				}

				ox = 0;
				oy += height;
			}

			// draw cursor
			if (cursorBlink()) {
				g.drawImage(font.getCharImage('_', paletteAdapter.getColor(computer.terminal.getTextColour()), s),
						m + (cw * computer.terminal.getCursorX()), m + (ch * computer.terminal.getCursorY()));
			}

			lastBlink = cursorBlink();
		}
	}

	@Override()
	public void onAdvance(double dt) {
		blinkLockedTime = Math.max(0, blinkLockedTime - dt);

		boolean repaint = lastBlink != cursorBlink();

		if (computer.terminal.getChanged()) {
			repaint = true;
			computer.terminal.clearChanged();
		}

		if (computer.terminal.getPalette().isChanged()) {
			repaint = true;
			computer.terminal.getPalette().clearChanged();
		}

		if (repaint) {
			this.redraw();
		}
	}

	private int[] coordsToCC(double x, double y) {
		return new int[] {
				1 + constrainToRange((int) Math.floor((x - margin.get()) / charWidth.get()), 0,
						computer.terminal.getWidth()),
				1 + constrainToRange((int) Math.floor((y - margin.get()) / charHeight.get()), 0,
						computer.terminal.getHeight()) };
	}

	private void mousePressed(MouseEvent e) {
		kbCap.requestFocus();
		int[] coords = coordsToCC(e.getX(), e.getY());
		computer.click(JFXMouseTranslator.toCC(e.getButton()), coords[0], coords[1], false);
	}

	private void mouseReleased(MouseEvent e) {
		int[] coords = coordsToCC(e.getX(), e.getY());
		computer.click(JFXMouseTranslator.toCC(e.getButton()), coords[0], coords[1], true);
	}

	private void mouseDragged(MouseEvent e) {
		int[] coords = coordsToCC(e.getX(), e.getY());

		if (lastDrag == null || lastDrag[0] != coords[0] || lastDrag[1] != coords[1]) {
			lastDrag = coords;
			computer.drag(JFXMouseTranslator.toCC(e.getButton()), coords[0], coords[1]);
		}
	}

	private void dragOver(DragEvent e) {
		if (e.getDragboard().hasFiles() || e.getDragboard().hasString()) {
			e.acceptTransferModes(TransferMode.COPY);
		}
	}

	/**
	 * Transfers the contents of the given {@link Clipboard} to the computer
	 * where applicable
	 * 
	 * @param cb
	 *            The {@link Clipboard} containing content to transfer
	 * @return Whether the contents were successfully transferred to the
	 *         computer or not
	 */
	public boolean transferContents(Clipboard cb) {
		if (cb.hasFiles()) {
			// copy files to computer root
			try {
				computer.copyFiles(cb.getFiles(), "/");

				val a = new Alert(AlertType.INFORMATION);
				a.setTitle("Files copied");
				a.setHeaderText("Files copied");
				a.setContentText("Files were successfully copied to computer ID " + computer.getID());
				a.initStyle(StageStyle.UTILITY);
				a.show();

				return true;
			} catch (IOException e1) {
				log.error("Error copying files {}", cb.getFiles(), e1);

				val a = new Alert(AlertType.ERROR);
				a.setTitle("File copy error");
				a.setHeaderText("File copy error");
				a.setContentText("There was an error copying file to computer ID " + computer.getID() + ":\n"
						+ e1.getLocalizedMessage() + "\n\nSee logs for more information");
				a.initStyle(StageStyle.UTILITY);
				a.show();

				return false;
			}
		} else if (cb.hasString()) {
			// paste text
			computer.paste(cb.getString());
			return true;
		} else {
			// no applicable transfer method
			return false;
		}
	}
}
