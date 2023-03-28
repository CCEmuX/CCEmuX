package net.clgd.ccemux.api.rendering;

import javax.annotation.Nonnull;

import dan200.computercraft.core.terminal.Palette;
import dan200.computercraft.core.util.Colour;
import net.clgd.ccemux.api.Utils;

/**
 * Wraps a {@link Palette} object with a given {@link ColorAdapter} to make it
 * easier to get color objects from the palette
 *
 * @param <C>
 * @author apemanzilla
 */
public final class PaletteAdapter<C> {
	/**
	 * The default color index for backgrounds
	 *
	 * @see Colour#BLACK
	 */
	public static final int DEFAULT_BACKGROUND = 0;

	/**
	 * The default color index for foregrounds
	 *
	 * @see Colour#WHITE
	 */
	public static final int DEFAULT_FOREGROUND = 15;

	/**
	 * An adapter used to generate a color object from RGB values
	 *
	 * @param <T> The type of object created
	 * @author apemanzilla
	 */
	@FunctionalInterface
	public interface ColorAdapter<T> {
		/**
		 * Creates a color object from the given RGB values, doubles on the range [0, 1]
		 *
		 * @param r The intensity of the red channel, between 0 and 1.
		 * @param g The intensity of the green channel, between 0 and 1.
		 * @param b The intensity of the blue channel, between 0 and 1.
		 * @return The converted color.
		 */
		T rgb(double r, double g, double b);
	}

	private final Palette palette;
	private final ColorAdapter<C> adapter;

	/**
	 * Creates a color object using the given RGB values (Equivalent to {@code getAdapter().rgb(r,g,b)}
	 *
	 * @param r The intensity of the red channel, between 0 and 1.
	 * @param g The intensity of the green channel, between 0 and 1.
	 * @param b The intensity of the clue channel, between 0 and 1.
	 * @return The converted color.
	 */
	public C getColor(double r, double g, double b) {
		return adapter.rgb(r, g, b);
	}

	/**
	 * Creates a color object using the given color from the palette
	 *
	 * @param c The numeric index of the terminal color
	 * @return The converted color
	 */
	public C getColor(int c) {
		return getColor(c, DEFAULT_BACKGROUND);
	}

	/**
	 * Creates a color object using the given color from the palette
	 *
	 * @param c   The numeric index of the terminal color
	 * @param def The default color index if none exists for {@code c}.
	 * @return The converted color
	 */
	public C getColor(int c, int def) {
		double[] col = c >= 0 && c <= 15 ? palette.getColour(15 - c) : palette.getColour(def);
		return getColor(
			Utils.constrainToRange(col[0], 0, 1),
			Utils.constrainToRange(col[1], 0, 1),
			Utils.constrainToRange(col[2], 0, 1)
		);
	}

	/**
	 * Creates a color object using the given color from the palette
	 *
	 * @param c A hexadecimal terminal color
	 * @return The converted color
	 */
	public C getColor(char c) {
		return getColor(Utils.base16ToInt(c));
	}

	/**
	 * Creates a color object using the given color from the palette
	 *
	 * @param c   A hexadecimal terminal color
	 * @param def The default color index if none exists for {@code c}.
	 * @return The converted color
	 */
	public C getColor(char c, int def) {
		return getColor(Utils.base16ToInt(c), def);
	}

	public PaletteAdapter(@Nonnull Palette palette, @Nonnull ColorAdapter<C> adapter) {
		this.palette = palette;
		this.adapter = adapter;
	}

	@Nonnull
	public Palette getPalette() {
		return palette;
	}

	@Nonnull
	public ColorAdapter<C> getAdapter() {
		return adapter;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PaletteAdapter<?> that = (PaletteAdapter<?>) o;
		if (!palette.equals(that.palette)) return false;
		return adapter.equals(that.adapter);
	}

	@Override
	public int hashCode() {
		int result = palette.hashCode();
		result = 31 * result + adapter.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "PaletteAdapter(palette=" + getPalette() + ", adapter=" + getAdapter() + ")";
	}
}
