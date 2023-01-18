package net.clgd.ccemux.api.rendering;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a font that can be used when rendering CC terminals
 *
 * @author apemanzilla
 */
public abstract class TerminalFont {
	private static final Logger log = LoggerFactory.getLogger(TerminalFont.class);
	public static final String FONT_RESOURCE_PATH = "img/term_font.png";
	public static final int BASE_WIDTH = 256;
	public static final int BASE_HEIGHT = 256;
	public static final int BASE_CHAR_WIDTH = 6;
	public static final int BASE_CHAR_HEIGHT = 9;
	public static final int BASE_MARGIN = 1;
	public static final int COLUMNS = 16;
	public static final int ROWS = 16;
	private static final Set<URL> registeredFonts = ConcurrentHashMap.newKeySet();

	public static void registerFont(@Nonnull URL url) {
		registeredFonts.add(url);
	}

	public static void loadImplicitFonts(@Nonnull ClassLoader loader) throws IOException {
		log.debug("Loading implicit fonts (from classloader {}", loader);
		final java.util.Enumeration<java.net.URL> res = loader.getResources(FONT_RESOURCE_PATH);
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
	 * @param loader     The loader to load fonts with
	 * @param comparator The comparator to determine which font is best
	 * @return The best registered font
	 * @throws IllegalStateException Thrown when no fonts can be loaded (either none are
	 *                               registered or all throw exceptions when loaded)
	 */
	@Nonnull
	public static <T extends TerminalFont> T getBest(@Nonnull Loader<T> loader, @Nonnull Comparator<? super T> comparator) {
		return registeredFonts.stream().map(u -> loader.loadFontSafe(u, e -> log.error("Error loading font from {}", u, e))).flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty)).min(comparator).orElseThrow(() -> new IllegalStateException("No fonts available"));
	}

	/**
	 * Loads and returns the best registered font, as determined by the
	 * resolution (highest combined vertical/horizontal resolution is best)
	 *
	 * @param loader The loader to load fonts with
	 * @return The best registered font
	 * @throws IllegalStateException Thrown when no fonts can be loaded (either none are
	 *                               registered or all throw exceptions when loaded)
	 */
	@Nonnull
	public static <T extends TerminalFont> T getBest(@Nonnull Loader<T> loader) {
		return getBest(loader, Comparator.comparingDouble(f -> -(f.getHorizontalScale() + f.getVerticalScale())));
	}


	/**
	 * A loader that can load a generic {@link TerminalFont} from a given
	 * {@link URL}
	 *
	 * @param <T>
	 * @author apemanzilla
	 */
	@FunctionalInterface
	public interface Loader<T extends TerminalFont> {
		/**
		 * Loads the font from the given URL
		 *
		 * @param url The URL of the font
		 * @return The loaded font
		 * @throws IOException If the font could not be loaded
		 */
		@Nonnull
		T loadFont(@Nonnull URL url) throws IOException;

		/**
		 * Loads the font from the given URL, with a consumer to handle thrown
		 * exceptions
		 *
		 * @param url     The URL of the font to load
		 * @param catcher The function to call if an exception is thrown
		 * @return The loaded font, or an empty optional if an exception was
		 * thrown
		 */
		@Nonnull
		default Optional<T> loadFontSafe(@Nonnull URL url, @Nonnull Consumer<? super Exception> catcher) {
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
	private final double horizontalScale;
	private final double verticalScale;
	/**
	 * The scaled character dimensions
	 */
	private final int charWidth;
	private final int charHeight;
	/**
	 * The margin around each character
	 */
	private final int margin;

	/**
	 * Creates a new terminal font
	 *
	 * @param imageWidth  The width of the font image.
	 * @param imageHeight The height of the font image.
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
	 * @param c The character
	 * @return The coordinates and dimensions of a given character
	 */
	@Nonnull
	public Rectangle getCharCoords(char c) {
		int charcode = c;
		return new Rectangle(margin + charcode % COLUMNS * (getCharWidth() + margin * 2), margin + charcode / ROWS * (getCharHeight() + margin * 2), getCharWidth(), getCharHeight());
	}

	/**
	 * The horizontal scale of the font, calculated based on base image resolution
	 */
	public double getHorizontalScale() {
		return this.horizontalScale;
	}

	/**
	 * The horizontal scale of the font, calculated based on base image resolution
	 */
	public double getVerticalScale() {
		return this.verticalScale;
	}

	/**
	 * The scaled character width
	 */
	public int getCharWidth() {
		return this.charWidth;
	}

	/**
	 * The scaled character height
	 */
	public int getCharHeight() {
		return this.charHeight;
	}

	/**
	 * The margin around each character
	 */
	public int getMargin() {
		return this.margin;
	}
}
