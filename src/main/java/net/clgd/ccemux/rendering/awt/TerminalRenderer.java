package net.clgd.ccemux.rendering.awt;

import java.awt.*;
import java.awt.image.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import net.clgd.ccemux.api.Utils;
import net.clgd.ccemux.api.rendering.PaletteAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders a terminal to arbitrary {@link java.awt.Graphics} objects. This is suitable both for taking
 * screenshots and rendering to the screen.
 */
public class TerminalRenderer {
	private static final Logger log = LoggerFactory.getLogger(TerminalRenderer.class);

	private static final char CURSOR_CHAR = '_';

	private static final PaletteAdapter.ColorAdapter<Color> AWT_COLOR_ADAPTER = (r, g, b) -> new Color((float) r, (float) g, (float) b);

	private final PaletteAdapter<Color> paletteCacher;

	private final Terminal terminal;
	private final int pixelWidth;
	private final int pixelHeight;
	final int margin;

	private static final Cache<CharImageRequest, BufferedImage> charImgCache = CacheBuilder.newBuilder()
		.expireAfterAccess(10, TimeUnit.SECONDS).build();

	public TerminalRenderer(Terminal terminal, double termScale) {
		this.pixelWidth = (int) (6 * termScale);
		this.pixelHeight = (int) (9 * termScale);
		this.margin = (int) (2 * termScale);
		this.terminal = terminal;
		this.paletteCacher = new PaletteAdapter<>(terminal.getPalette(), AWT_COLOR_ADAPTER);
	}

	public int getMargin() {
		return margin;
	}

	/**
	 * Get the requested size of the terminal.
	 *
	 * Note, you should probably keep a lock on the renderer's terminal
	 *
	 * @return The terminal's size.
	 */
	public Dimension getSize() {
		return new Dimension(
			terminal.getWidth() * pixelWidth + margin * 2,
			terminal.getHeight() * pixelHeight + margin * 2
		);
	}

	private void drawChar(AWTTerminalFont font, Graphics g, char c, int x, int y, int color) {
		if (c == '\0' || Character.isSpaceChar(c)) return;

		Rectangle r = font.getCharCoords(c);
		Color colour = paletteCacher.getColor(color, PaletteAdapter.DEFAULT_FOREGROUND);

		BufferedImage charImg = null;

		float[] zero = new float[4];

		try {
			charImg = charImgCache.get(new CharImageRequest(c, colour, font), () -> {
				float[] rgb = new float[4];
				colour.getRGBComponents(rgb);

				RescaleOp rop = new RescaleOp(rgb, zero, null);

				GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();

				BufferedImage img = font.getBitmap().getSubimage(r.x, r.y, r.width, r.height);
				BufferedImage pixel = gc.createCompatibleImage(r.width, r.height, Transparency.TRANSLUCENT);

				Graphics ig = pixel.getGraphics();
				ig.drawImage(img, 0, 0, null);
				ig.dispose();

				rop.filter(pixel, pixel);
				return pixel;
			});
		} catch (ExecutionException e) {
			log.error("Could not retrieve char image from cache!", e);
		}

		g.drawImage(charImg, x, y, pixelWidth, pixelHeight, null);
	}

	public void render(AWTTerminalFont font, Graphics g) {
		int dx = 0;
		int dy = 0;

		for (int y = 0; y < terminal.getHeight(); y++) {
			TextBuffer textLine = terminal.getLine(y);
			TextBuffer bgLine = terminal.getBackgroundColourLine(y);
			TextBuffer fgLine = terminal.getTextColourLine(y);

			int height = (y == 0 || y == terminal.getHeight() - 1) ? pixelHeight + margin : pixelHeight;

			for (int x = 0; x < terminal.getWidth(); x++) {
				int width = (x == 0 || x == terminal.getWidth() - 1) ? pixelWidth + margin : pixelWidth;

				g.setColor(paletteCacher.getColor(bgLine == null ? 'f' : bgLine.charAt(x), PaletteAdapter.DEFAULT_BACKGROUND));
				g.fillRect(dx, dy, width, height);

				char character = (textLine == null) ? ' ' : textLine.charAt(x);
				char fgChar = (fgLine == null) ? ' ' : fgLine.charAt(x);

				drawChar(font, g, character, x * pixelWidth + margin, y * pixelHeight + margin, Utils.base16ToInt(fgChar));

				dx += width;
			}

			dx = 0;
			dy += height;
		}

		if (terminal.getCursorBlink() && Utils.getGlobalCursorBlink()) {
			drawChar(font, g, CURSOR_CHAR, terminal.getCursorX() * pixelWidth + margin,
				terminal.getCursorY() * pixelHeight + margin, terminal.getTextColour());
		}
	}

	private record CharImageRequest(char character, @Nonnull Color color, @Nonnull AWTTerminalFont font) {
	}
}
