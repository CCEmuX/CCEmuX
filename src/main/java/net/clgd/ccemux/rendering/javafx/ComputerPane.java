package net.clgd.ccemux.rendering.javafx;

import static net.clgd.ccemux.rendering.TerminalFont.BASE_CHAR_HEIGHT;
import static net.clgd.ccemux.rendering.TerminalFont.BASE_CHAR_WIDTH;
import static net.clgd.ccemux.rendering.TerminalFont.BASE_MARGIN;

import dan200.computercraft.core.terminal.TextBuffer;
import javafx.application.Platform;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;
import net.clgd.ccemux.Utils;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.PaletteAdapter;

@Value
@EqualsAndHashCode(callSuper = false)
public class ComputerPane extends Pane implements EmulatedComputer.Listener {
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

	private double blinkLockedTime = 0;

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

		// setup listeners
		setOnKeyPressed(this::keyPressed);
		setOnKeyReleased(this::keyReleased);
		setOnKeyTyped(this::keyTyped);

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
			val g = canvas.getGraphicsContext2D();

			// cache some important values as primitives
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
					g.drawImage(font.getCharImage(text.charAt(x), paletteAdapter.getColor(fg.charAt(x))),
							ox + (x == 0 ? m : 0), oy + (y == 0 ? m : 0));

					ox += width;
				}

				ox = 0;
				oy += height;
			}
		}
	}

	private void keyTyped(KeyEvent e) {
		char c = e.getCharacter().charAt(0);
		if (Utils.isPrintableChar(c)) {
			computer.pressChar(e.getCharacter().charAt(0));
		}
	}

	private void keyPressed(KeyEvent e) {
		computer.pressKey(JFXKeyTranslator.translateToCC(e.getCode()), false);
	}

	private void keyReleased(KeyEvent e) {
		computer.pressKey(JFXKeyTranslator.translateToCC(e.getCode()), true);
	}

	@Override
	public void onAdvance(double dt) {
		boolean repaint = false;

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
}
