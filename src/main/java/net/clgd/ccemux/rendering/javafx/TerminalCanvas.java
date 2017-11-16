package net.clgd.ccemux.rendering.javafx;

import static net.clgd.ccemux.rendering.TerminalFont.BASE_CHAR_HEIGHT;
import static net.clgd.ccemux.rendering.TerminalFont.BASE_CHAR_WIDTH;
import static net.clgd.ccemux.rendering.TerminalFont.BASE_MARGIN;

import dan200.computercraft.core.terminal.TextBuffer;
import javafx.application.Platform;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;
import net.clgd.ccemux.emulation.EmulatedTerminal;
import net.clgd.ccemux.rendering.PaletteAdapter;

@Value
@EqualsAndHashCode(callSuper = false)
public class TerminalCanvas extends Pane {
	private final Canvas canvas;
	private final EmulatedTerminal terminal;
	private final JFXTerminalFont font;
	private final PaletteAdapter<Color> paletteAdapter;
	private final ReadOnlyDoubleProperty termScale;

	private final DoubleExpression margin;
	private final DoubleExpression charWidth;
	private final DoubleExpression charHeight;
	private final DoubleExpression totalWidth;
	private final DoubleExpression totalHeight;

	public TerminalCanvas(EmulatedTerminal terminal, JFXTerminalFont font, ReadOnlyDoubleProperty termScale) {
		this.terminal = terminal;
		this.font = font;
		this.paletteAdapter = new PaletteAdapter<>(terminal.getPalette(), Color::color);
		this.termScale = termScale;

		this.margin = termScale.multiply(BASE_MARGIN);
		this.charWidth = termScale.multiply(BASE_CHAR_WIDTH);
		this.charHeight = termScale.multiply(BASE_CHAR_HEIGHT);
		this.totalWidth = margin.multiply(2).add(charWidth.multiply(terminal.getWidth()));
		this.totalHeight = margin.multiply(2).add(charHeight.multiply(terminal.getHeight()));

		this.prefWidthProperty().bind(totalWidth);
		this.prefHeightProperty().bind(totalHeight);

		this.canvas = new Canvas(totalWidth.get(), totalHeight.get());
		canvas.widthProperty().bind(this.widthProperty());
		canvas.heightProperty().bind(this.heightProperty());

		canvas.widthProperty().addListener(o -> this.redraw());
		canvas.heightProperty().addListener(o -> this.redraw());

		this.getChildren().add(canvas);
	}

	@Override
	public boolean isResizable() {
		// makes sure that this element is drawn with a rigid size and not
		// resized by parents
		return true;
	}

	private void redraw() {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(this::redraw);
			return;
		}
		
		synchronized (terminal) {
			val g = canvas.getGraphicsContext2D();

			// cache some important values as primitives
			double m = margin.get();
			double cw = charWidth.get(), ch = charHeight.get();
			int tw = terminal.getWidth(), th = terminal.getHeight();

			// current position offsets
			double ox = 0, oy = 0;

			// height/width of current position
			double height, width;

			TextBuffer bg, fg, text;

			for (int y = 0; y < th; y++) {
				height = ch + ((y == 0 || y == th - 1) ? m : 0);

				bg = terminal.getBackgroundColourLine(y);
				fg = terminal.getTextColourLine(y);
				text = terminal.getLine(y);

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
	
	public void tick() {
		boolean repaint = false;
		
		if (terminal.getChanged()) {
			repaint = true;
			terminal.clearChanged();
		}
		
		if (terminal.getPalette().isChanged()) {
			repaint = true;
			terminal.getPalette().clearChanged();
		}
		
		if (repaint) {
			this.redraw();
		}
	}
}
