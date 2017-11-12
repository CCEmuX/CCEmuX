package net.clgd.ccemux.rendering;

import dan200.computercraft.shared.util.Palette;
import lombok.Value;
import net.clgd.ccemux.Utils;

/**
 * Wraps a {@link Palette} object with a given {@link ColorAdapter} to make it
 * easier to get color objects from the palette
 * 
 * @author apemanzilla
 *
 * @param <C>
 */
@Value
public class PaletteAdapter<C> {
	/**
	 * An adapter used to generate a color object from RGB values
	 * 
	 * @author apemanzilla
	 *
	 * @param <T>
	 *            The type of object created
	 */
	@FunctionalInterface
	public static interface ColorAdapter<T> {
		/**
		 * Creates a color object from the given RGB values, doubles on the
		 * range [0, 1]
		 * 
		 * @param r
		 * @param g
		 * @param b
		 * @return
		 */
		public T rgb(double r, double g, double b);
	}

	private final Palette palette;
	private final ColorAdapter<C> adapter;

	/**
	 * Creates a color object using the given RGB values (Equivalent to
	 * <code>getAdapter().rgb(r,g,b)</code>)
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public C getColor(double r, double g, double b) {
		return adapter.rgb(r, g, b);
	}

	/**
	 * Creates a color object using the given color from the palette
	 * 
	 * @param c
	 * @return
	 */
	public C getColor(int c) {
		double[] col;

		if ((col = palette.getColour(15 - c)) == null) {
			col = palette.getColour(0);
		}

		col = Utils.clampColor(col);
		return getColor(col[0], col[1], col[2]);
	}

	/**
	 * Creates a color object using the given color from the palette
	 * 
	 * @param c
	 * @return
	 */
	public C getColor(char c) {
		return getColor(Utils.base16ToInt(c));
	}
}
