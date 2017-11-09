package net.clgd.ccemux.rendering;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.Utils;

@Slf4j
public class TerminalFont implements Serializable, Comparable<TerminalFont> {
	private static final long serialVersionUID = -2121299756211115254L;

	public static final String FONT_RESOURCE_PATH = "assets/computercraft/textures/gui/term_font.png";

	public static final int BASE_WIDTH = 256, BASE_HEIGHT = 256;

	public static final int BASE_CHAR_WIDTH = 6, BASE_CHAR_HEIGHT = 9;
	public static final int BASE_MARGIN = 1;
	public static final int COLUMNS = 16, ROWS = 16;

	/**
	 * A set of fonts that have been registered explicitly. (presumably by a
	 * plugin) These fonts will have priority over implicit fonts.
	 */
	@Getter
	private static final Set<TerminalFont> explicitFonts = new HashSet<>();

	/**
	 * A set of fonts that have been loaded implicitly, from the CC jar or
	 * resource packs usually.
	 */
	@Getter
	private static final Set<TerminalFont> implicitFonts = new HashSet<>();

	/**
	 * Locates fonts not explicitly registered, but present at the standard path
	 * (e.g. from resource packs)
	 *
	 * @return
	 * @throws IOException
	 */
	public static void loadImplicitFonts() throws IOException {
		log.debug("Loading implicit terminal fonts");

		val urls = TerminalFont.class.getClassLoader().getResources(FONT_RESOURCE_PATH);
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			try {
				TerminalFont font = new TerminalFont(url);

				implicitFonts.add(font);
				log.debug("Loaded implicit terminal font from {}", url);
			} catch (IOException e) {
				log.error("Failed to load implicit terminal font from {}", url, e);
			}
		}
	}

	/**
	 * Explicitly registers the given font, giving it priority over implicitly
	 * loaded fonts.
	 *
	 * @param font
	 */
	public static void registerFont(TerminalFont font) {
		explicitFonts.add(font);
	}

	/**
	 * Gets the "best" terminal font. This is determined first by checking for
	 * explicitly registered fonts (which always have priority over implicitly
	 * loaded fonts) and then, if multiple candidates are available, sorting by
	 * the font scale (higher scale being better).
	 *
	 * @return The "best" font
	 */
	public static TerminalFont getBest() {
		val fonts = explicitFonts.size() > 0 ? explicitFonts : implicitFonts;
		return fonts.stream().sorted().reduce((a, b) -> b)
				.orElseThrow(() -> new RuntimeException("No terminal fonts available"));
	}

	/**
	 * The font image
	 */
	@Getter
	private final BufferedImage base;

	/**
	 * The scale of the font, calculated based on base image resolution
	 */
	@Getter
	private final double horizontalScale, verticalScale;

	/**
	 * The scaled character dimensions
	 */
	@Getter
	private final int charWidth, charHeight;

	/**
	 * The margin around each character
	 */
	@Getter
	private final int margin;

	/**
	 * Copies of the font image with different colors
	 */
	private final Cache<Color, BufferedImage> tinted = CacheBuilder.newBuilder()
			.initialCapacity(16)
			.maximumSize(64)
			.expireAfterAccess(30, TimeUnit.SECONDS)
			.build();

	/**
	 * Creates a new terminal font
	 *
	 * @param base
	 *            The base image for this font
	 */
	public TerminalFont(BufferedImage base) {
		this.base = base;

		horizontalScale = base.getWidth() / (double) BASE_WIDTH;
		verticalScale = base.getHeight() / (double) BASE_HEIGHT;

		margin = (int)Math.round(BASE_MARGIN * horizontalScale);
		charWidth = (int) Math.round(BASE_CHAR_WIDTH * horizontalScale);
		charHeight = (int) Math.round(BASE_CHAR_HEIGHT * verticalScale);
	}

	/**
	 * Creates a new terminal font
	 *
	 * @param url
	 *            The URL to load the base image from
	 * @throws IOException
	 *             Thrown if there's an exception loading the base image
	 */
	public TerminalFont(URL url) throws IOException {
		this(ImageIO.read(url.openStream()));
	}

	/**
	 * Gets the scaled coordinates and dimensions for a given character in this
	 * font
	 *
	 * @param c
	 *            The character
	 * @return The coordinates and dimensions of a given character
	 */
	public Rectangle getCharCoords(char c) {
		int charcode = (int) c;
		return new Rectangle(
				margin + charcode % COLUMNS * (getCharWidth() + margin * 2),
				margin + charcode / ROWS * (getCharHeight() + margin * 2),
				getCharWidth(), getCharHeight()
		);
	}

	/**
	 * Gets a copy of this font, colored accordingly
	 *
	 * @param col
	 *            The color to use
	 * @return A colorized copy of this font
	 */
	public BufferedImage getTinted(Color col) {
		try {
			return tinted.get(col, () -> Utils.makeTintedCopy(base, col));
		} catch (ExecutionException e) {
			log.error("Failed to get tinted font for color {}", col, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TerminalFont)) return false;

		BufferedImage base = getBase();
		BufferedImage base2 = ((TerminalFont) o).getBase();

		if (base.getHeight() != base2.getHeight() || base.getWidth() != base2.getWidth()) return false;
		int w = base.getWidth(), h = base.getHeight();

		// compare pixel by pixel
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				if (base.getRGB(x, y) != base2.getRGB(x, y)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		try {
			val bos = new ByteArrayOutputStream();
			ImageIO.write(base, "png", bos);
			return Arrays.hashCode(bos.toByteArray());
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public int compareTo(TerminalFont o) {
		return (getCharWidth() + getCharHeight()) - (o.getCharWidth() + o.getCharHeight());
	}
}
