package net.clgd.ccemux.api.rendering;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Represents a font that can be used when rendering CC terminals
 *
 * @author apemanzilla
 *
 */
@Slf4j
public abstract class TerminalFont {
	public static final String FONT_RESOURCE_PATH = "assets/computercraft/textures/gui/term_font.png";

	public static final int BASE_WIDTH = 256, BASE_HEIGHT = 256;

	public static final int BASE_CHAR_WIDTH = 6, BASE_CHAR_HEIGHT = 9;
	public static final int BASE_MARGIN = 1;
	public static final int COLUMNS = 16, ROWS = 16;

	private static final Set<URL> registeredFonts = ConcurrentHashMap.newKeySet();

	public static void registerFont(URL url) {
		registeredFonts.add(url);
	}

	public static void loadImplicitFonts(ClassLoader loader) throws IOException {
		log.debug("Loading implicit fonts (from classloader {}", loader);

		val res = loader.getResources(FONT_RESOURCE_PATH);

		while (res.hasMoreElements()) {
			URL u = res.nextElement();
			log.info("Registering implicit font from {}", u);
			registerFont(u);
		}
	}

	/**
	 * Loads and returns the best registered font, as determined by a given
	 * comparator.
	 *
	 * @param loader
	 *            The loader to load fonts with
	 * @param comparator
	 *            The comparator to determine which font is best
	 * @return The best registered font
	 * @throws IllegalStateException
	 *             Thrown when no fonts can be loaded (either none are
	 *             registered or all throw exceptions when loaded)
	 */
	public static <T extends TerminalFont> T getBest(Loader<T> loader, Comparator<? super T> comparator) {
		return registeredFonts.stream()
				.map(u -> loader.loadFontSafe(u, e -> log.error("Error loading font from {}", u, e)))
				.flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty)).sorted(comparator).findFirst()
				.orElseThrow(() -> new IllegalStateException("No fonts available"));
	}

	/**
	 * Loads and returns the best registered font, as determined by the
	 * resolution (highest combined vertical/horizontal resolution is best)
	 *
	 * @param loader
	 *            The loader to load fonts with
	 * @return The best registered font
	 * @throws IllegalStateException
	 *             Thrown when no fonts can be loaded (either none are
	 *             registered or all throw exceptions when loaded)
	 */
	public static <T extends TerminalFont> T getBest(Loader<T> loader) {
		return getBest(loader, Comparator.comparingDouble(f -> -(f.getHorizontalScale() + f.getVerticalScale())));
	}

	/**
	 * A loader that can load a generic {@link TerminalFont} from a given
	 * {@link URL}
	 *
	 * @author apemanzilla
	 *
	 * @param <T>
	 */
	@FunctionalInterface
	public static interface Loader<T extends TerminalFont> {
		/**
		 * Loads the font from the given URL
		 *
		 * @param url
		 * @return
		 * @throws Exception
		 */
		public T loadFont(URL url) throws Exception;

		/**
		 * Loads the font from the given URL, with a consumer to handle thrown
		 * exceptions
		 *
		 * @param url
		 * @param catcher
		 * @return The loaded font, or an empty optional if an exception was
		 *         thrown
		 */
		public default Optional<T> loadFontSafe(URL url, Consumer<? super Exception> catcher) {
			try {
				return Optional.of(loadFont(url));
			} catch (Exception e) {
				catcher.accept(e);
				return Optional.empty();
			}
		}
	}

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
	 * Creates a new terminal font
	 *
	 * @param imageWidth
	 *            The width of the font image.
	 * @param imageHeight
	 *            The height of the font image.
	 */
	public TerminalFont(int imageWidth, int imageHeight) {
		horizontalScale = imageWidth / (double) BASE_WIDTH;
		verticalScale = imageHeight / (double) BASE_HEIGHT;

		margin = (int) Math.round(BASE_MARGIN * horizontalScale);
		charWidth = (int) Math.round(BASE_CHAR_WIDTH * horizontalScale);
		charHeight = (int) Math.round(BASE_CHAR_HEIGHT * verticalScale);
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
		return new Rectangle(margin + charcode % COLUMNS * (getCharWidth() + margin * 2),
				margin + charcode / ROWS * (getCharHeight() + margin * 2), getCharWidth(), getCharHeight());
	}
}
