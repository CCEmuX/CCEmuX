package net.clgd.ccemux.rendering;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Slf4j
public class TerminalFonts<RendererT extends Renderer> {
	private static final Map<Renderer, TerminalFonts<? extends Renderer>> rendererFontsMap = new HashMap<>();

	@SuppressWarnings({"unchecked"})
	public static <R extends Renderer> TerminalFonts<R> getFontsFor(R renderer) {
		if (rendererFontsMap.containsKey(renderer)) {
			return (TerminalFonts<R>) rendererFontsMap.get(renderer);
		} else {
			val fonts = new TerminalFonts<R>();

			try {
				fonts.loadImplicitFonts(renderer);
			} catch (IOException e) {
				log.error("Failed to load implicit fonts for renderer {}", renderer.getClass().getName(), e);
				e.printStackTrace();
			}

			rendererFontsMap.put(renderer, fonts);
			return fonts;
		}
	}

	/**
	 * A set of fonts that have been registered explicitly. (presumably by a
	 * plugin) These fonts will have priority over implicit fonts.
	 */
	@Getter
	private final Set<TerminalFont> explicitFonts = new HashSet<>();

	/**
	 * A set of fonts that have been loaded implicitly, from the CC jar or
	 * resource packs usually.
	 */
	@Getter
	private final Set<TerminalFont> implicitFonts = new HashSet<>();

	public static void loadAndRegisterFont(InputStream stream) throws IOException {
		for (val entry : rendererFontsMap.entrySet()) {
			val font = entry.getKey().loadFont(stream);
			entry.getValue().registerFont(font);
		}
	}

	/**
	 * Locates fonts not explicitly registered, but present at the standard path
	 * (e.g. from resource packs)
	 *
	 * @return
	 * @throws IOException
	 */
	private void loadImplicitFonts(RendererT renderer) throws IOException {
		log.debug("Loading implicit terminal fonts");

		val urls = TerminalFont.class.getClassLoader().getResources(TerminalFont.FONT_RESOURCE_PATH);
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			try {
				TerminalFont font = renderer.loadFont(url.openStream());
				implicitFonts.add(font);
				log.debug("Loaded implicit terminal font from {}", url);
			} catch (IOException e) {
				log.error("Failed to load implicit terminal font from {}", url, e);
			}
		}
	}

	private void registerFont(TerminalFont font) {
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
	public TerminalFont getBest() {
		val fonts = explicitFonts.size() > 0 ? explicitFonts : implicitFonts;
		return fonts.stream()
				.sorted(Comparator.comparingInt(a -> a.getCharWidth() + a.getCharHeight()))
				.reduce((a, b) -> b)
				.orElseThrow(() -> new RuntimeException("No terminal fonts available"));
	}
}
