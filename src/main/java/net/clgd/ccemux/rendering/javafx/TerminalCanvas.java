package net.clgd.ccemux.rendering.javafx;

import static net.clgd.ccemux.rendering.TerminalFont.*;

import dan200.computercraft.core.terminal.TextBuffer;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import lombok.*;
import net.clgd.ccemux.emulation.EmulatedTerminal;
import net.clgd.ccemux.rendering.PaletteAdapter;

@Value
@EqualsAndHashCode(callSuper = false)
public class TerminalCanvas extends Canvas {
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

		this.widthProperty().bind(totalWidth);
		this.heightProperty().bind(totalHeight);
	}

	@Override
	public boolean isResizable() {
		// makes sure that this element is drawn with a rigid size and not
		// resized by parents
		return false;
	}

	public void redraw() {		
		val g = this.getGraphicsContext2D();

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
				g.drawImage(font.getCharImage(text.charAt(x), paletteAdapter.getColor(fg.charAt(x))), ox, oy);

				ox += width;
			}

			oy += height;
		}
		
		System.out.println("Drawn");
	}
}
