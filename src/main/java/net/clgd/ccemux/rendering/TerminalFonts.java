package net.clgd.ccemux.rendering;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TerminalFonts {
	private static final Map<Class<? extends Renderer>, TerminalFonts> rendererFontsMap = new HashMap<>();

	public static TerminalFonts getFontsFor(Class<? extends Renderer> renderer) {
		if (rendererFontsMap.containsKey(renderer)) {
			return rendererFontsMap.get(renderer);
		} else {
			TerminalFonts fonts = new TerminalFonts();
			rendererFontsMap.put(renderer, fonts);
			return fonts;
		}
	}

	private TerminalFont bestFont;
	private boolean bestFontInvalidated = true;

	/**
	 * A set of fonts that have been registered explicitly. (presumably by a
	 * plugin) These fonts will have priority over implicit fonts.
	 */
	@Getter
	private static final Set<URL> explicitFonts = new HashSet<>();

	/**
	 * A set of fonts that have been loaded implicitly, from the CC jar or
	 * resource packs usually.
	 */
	@Getter
	private static final Set<URL> implicitFonts = new HashSet<>();

	public static void registerFont(URL url) throws IOException {
		explicitFonts.add(url);

		for (TerminalFonts fonts : rendererFontsMap.values()) {
			fonts.bestFontInvalidated = true;
		}
	}

	public static void loadImplicitFonts() throws IOException {
		log.debug("Loading implicit terminal fonts");

		val urls = TerminalFont.class.getClassLoader().getResources(TerminalFont.FONT_RESOURCE_PATH);

		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			implicitFonts.add(url);
			log.debug("Loaded implicit terminal font from {}", url);
		}

		for (TerminalFonts fonts : rendererFontsMap.values()) {
			fonts.bestFontInvalidated = true;
		}
	}

	/**
	 * Gets the "best" terminal font. This is determined first by checking for
	 * explicitly registered fonts (which always have priority over implicitly
	 * loaded fonts) and then, if multiple candidates are available, sorting by
	 * the font scale (higher scale being better).
	 *
	 * @return The "best" font
	 */
	public TerminalFont getBest(Renderer renderer) {
		if (bestFontInvalidated) {
			List<TerminalFont> explicit = explicitFonts.stream().map(url -> {
					try {
						return renderer.loadFont(url);
					} catch (IOException e) {
						log.error("Failed to load explicit font {}", url, e);
					}

					return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

			List<TerminalFont> implicit = implicitFonts.stream().map(url -> {
					try {
						return renderer.loadFont(url);
					} catch (IOException e) {
						log.error("Failed to load implicit font {}", url, e);
					}

					return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

			val fonts = !explicit.isEmpty() ? explicit : implicit;

			bestFont = fonts.stream()
					.sorted(Comparator.comparingInt(a -> a.getCharWidth() + a.getCharHeight()))
					.reduce((a, b) -> b)
					.orElseThrow(() -> new RuntimeException("No terminal fonts available"));

			bestFontInvalidated = false;
		}

		return bestFont;
	}
}
