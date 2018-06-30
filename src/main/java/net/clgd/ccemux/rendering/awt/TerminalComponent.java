package net.clgd.ccemux.rendering.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.api.Utils;
import net.clgd.ccemux.api.rendering.PaletteAdapter;
import net.clgd.ccemux.api.rendering.PaletteAdapter.ColorAdapter;

@Slf4j
class TerminalComponent extends Canvas {
	private static final long serialVersionUID = -5043543826280613143L;

	public static final ColorAdapter<Color> AWT_COLOR_ADAPTER = (r, g, b) -> new Color((float) r, (float) g, (float) b);

	private final PaletteAdapter<Color> paletteCacher;

	public final Terminal terminal;
	public final int pixelWidth;
	public final int pixelHeight;
	public final int margin;

	public char cursorChar = '_';

	public boolean blinkLocked = false;

	@Value
	public static class CharImageRequest {
		private char character;
		private Color color;
	}

	private final Cache<CharImageRequest, BufferedImage> charImgCache = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.SECONDS).build();

	public TerminalComponent(Terminal terminal, double termScale) {
		this.pixelWidth = (int) (6 * termScale);
		this.pixelHeight = (int) (9 * termScale);
		this.margin = (int) (2 * termScale);
		this.terminal = terminal;
		this.paletteCacher = new PaletteAdapter<>(terminal.getPalette(), AWT_COLOR_ADAPTER);

		resizeTerminal(terminal.getWidth(), terminal.getHeight());
	}

	public void resizeTerminal(int width, int height) {
		Dimension termDimensions = new Dimension(width * pixelWidth + margin * 2, height * pixelHeight + margin * 2);

		setSize(termDimensions);
		setPreferredSize(termDimensions);
	}

	private void drawChar(AWTTerminalFont font, Graphics g, char c, int x, int y, int color) {
		if (c == '\0' || Character.isSpaceChar(c))
			return; // nothing to do here

		Rectangle r = font.getCharCoords(c);
		Color colour = paletteCacher.getColor(color);

		BufferedImage charImg = null;

		float[] zero = new float[4];

		try {
			charImg = charImgCache.get(new CharImageRequest(c, colour), () -> {
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

	private void renderTerminal(AWTTerminalFont font, double dt) {
		synchronized (terminal) {
			Graphics g = getBufferStrategy().getDrawGraphics();

			int dx = 0;
			int dy = 0;

			for (int y = 0; y < terminal.getHeight(); y++) {
				TextBuffer textLine = terminal.getLine(y);
				TextBuffer bgLine = terminal.getBackgroundColourLine(y);
				TextBuffer fgLine = terminal.getTextColourLine(y);

				int height = (y == 0 || y == terminal.getHeight() - 1) ? pixelHeight + margin : pixelHeight;

				for (int x = 0; x < terminal.getWidth(); x++) {
					int width = (x == 0 || x == terminal.getWidth() - 1) ? pixelWidth + margin : pixelWidth;

					g.setColor(paletteCacher.getColor((bgLine == null) ? 'f' : bgLine.charAt(x)));
					g.fillRect(dx, dy, width, height);

					char character = (textLine == null) ? ' ' : textLine.charAt(x);
					char fgChar = (fgLine == null) ? ' ' : fgLine.charAt(x);

					drawChar(font, g, character, x * pixelWidth + margin, y * pixelHeight + margin,
							Utils.base16ToInt(fgChar));

					dx += width;
				}

				dx = 0;
				dy += height;
			}

			boolean blink = terminal.getCursorBlink() && (blinkLocked || Utils.getGlobalCursorBlink());

			if (blink) {
				drawChar(font, g, cursorChar, terminal.getCursorX() * pixelWidth + margin,
						terminal.getCursorY() * pixelHeight + margin, terminal.getTextColour());
			}

			g.dispose();
			// paletteCacher.setCurrentPalette(terminal.getPalette());
		}
	}

	public void render(AWTTerminalFont font, double dt) {
		if (getBufferStrategy() == null) {
			createBufferStrategy(2);
		}

		do {
			do {
				renderTerminal(font, dt);
			} while (getBufferStrategy().contentsRestored());

			getBufferStrategy().show();
		} while (getBufferStrategy().contentsLost());
	}
}
