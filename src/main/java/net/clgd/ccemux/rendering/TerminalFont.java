package net.clgd.ccemux.rendering;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.clgd.ccemux.Utils;

public class TerminalFont {
	private static final Logger log = LoggerFactory.getLogger(TerminalFont.class);

	public static final String FONT_RESOURCE_PATH = "assets/computercraft/textures/gui/termFont.png";

	public static final int BASE_CHAR_WIDTH = 6, BASE_CHAR_HEIGHT = 9;
	public static final int COLUMNS = 16, ROWS = 16;

	public static final List<TerminalFont> fonts = new ArrayList<TerminalFont>();

	public static void load() throws IOException {
		log.info("Loading terminal fonts");

		Enumeration<URL> urls = TerminalFont.class.getClassLoader().getResources(FONT_RESOURCE_PATH);
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			try {
				TerminalFont font = new TerminalFont(url);

				fonts.add(font);
				log.debug("Loaded font from {}", url);
			} catch (IOException e) {
				log.error("Failed to load font from {}", url, e);
			}
		}
	}

	/**
	 * Gets the best terminal font, determined by the scale (higher scale = better)
	 * @return
	 */
	public static TerminalFont getBest() {
		return fonts.stream().sorted((f1, f2) -> Double.compare(f2.getHorizontalScale() + f2.getVerticalScale(),
				f1.getHorizontalScale() + f1.getVerticalScale())).findFirst().orElse(null);
	}

	private final URL source;

	private final double horizontalScale, verticalScale;
	private final int charWidth, charHeight;

	private final BufferedImage base;

	private final BufferedImage[] tinted;

	private TerminalFont(URL url) throws IOException {
		source = url;

		base = ImageIO.read(url.openStream());

		horizontalScale = base.getWidth() / 256d;
		verticalScale = base.getHeight() / 256d;

		charWidth = (int) Math.round(BASE_CHAR_WIDTH * horizontalScale);
		charHeight = (int) Math.round(BASE_CHAR_HEIGHT * verticalScale);

		tinted = new BufferedImage[16];
		for (int i = 0; i < tinted.length; i++) {
			tinted[i] = Utils.makeTintedCopy(base, Utils.getCCColourFromInt(i));
		}
	}

	public Rectangle getCharCoords(char c) {
		int charcode = (int) c;
		return new Rectangle(charcode % COLUMNS * getCharWidth(), charcode / ROWS * getCharHeight(), getCharWidth(),
				getCharHeight());
	}

	public URL getSource() {
		return source;
	}

	public BufferedImage getBase() {
		return base;
	}

	public BufferedImage[] getTinted() {
		return tinted;
	}

	public double getHorizontalScale() {
		return horizontalScale;
	}

	public double getVerticalScale() {
		return verticalScale;
	}

	public int getCharWidth() {
		return charWidth;
	}

	public int getCharHeight() {
		return charHeight;
	}
}
