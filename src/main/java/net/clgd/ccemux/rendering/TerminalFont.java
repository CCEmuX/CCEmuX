package net.clgd.ccemux.rendering;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.Utils;

@Slf4j
public class TerminalFont {
	public static final String FONT_RESOURCE_PATH = "assets/computercraft/textures/gui/term_font.png";

	public static final int BASE_CHAR_WIDTH = 6, BASE_CHAR_HEIGHT = 9;
	public static final int COLUMNS = 16, ROWS = 16;

	@Getter
	private static final List<TerminalFont> fonts = new ArrayList<TerminalFont>();

	public static void loadFonts() throws IOException {
		log.debug("Loading terminal fonts");

		Enumeration<URL> urls = TerminalFont.class.getClassLoader().getResources(FONT_RESOURCE_PATH);
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			try {
				TerminalFont font = new TerminalFont(url);

				fonts.add(font);
				log.debug("Loaded terminal font from {}", url);
			} catch (IOException e) {
				log.error("Failed to load terminal font from {}", url, e);
			}
		}
	}

	/**
	 * Gets the best terminal font, determined by the scale compared to regular
	 * CC (higher scale = better)
	 * 
	 * @return
	 */
	public static TerminalFont getBest() {
		return fonts.stream().sorted((f1, f2) -> Double.compare(f2.getHorizontalScale() + f2.getVerticalScale(),
				f1.getHorizontalScale() + f1.getVerticalScale())).findFirst().orElse(null);
	}

	@Getter
	private final URL source;

	@Getter
	private final double horizontalScale, verticalScale;
	
	@Getter
	private final int charWidth, charHeight;

	@Getter
	private final BufferedImage base;

	private final HashMap<Color, BufferedImage> tinted;

	private TerminalFont(URL url) throws IOException {
		source = url;

		base = ImageIO.read(url.openStream());

		horizontalScale = base.getWidth() / 256d;
		verticalScale = base.getHeight() / 256d;

		charWidth = (int) Math.round(BASE_CHAR_WIDTH * horizontalScale);
		charHeight = (int) Math.round(BASE_CHAR_HEIGHT * verticalScale);

		tinted = new HashMap<>();
		for (int i = 0; i < 16; i++) {
			Color tint = Utils.getCCColourFromInt(i);
			tinted.put(tint, Utils.makeTintedCopy(base, tint));
		}
	}

	public Rectangle getCharCoords(char c) {
		int charcode = (int) c;
		return new Rectangle(charcode % COLUMNS * getCharWidth(), charcode / ROWS * getCharHeight(), getCharWidth(),
				getCharHeight());
	}

	public BufferedImage getTinted(Color col) {
		if (tinted.containsKey(col)) {
			return tinted.get(col);
		} else {
			return tinted.put(col, Utils.makeTintedCopy(base, col));
		}
	}
}
