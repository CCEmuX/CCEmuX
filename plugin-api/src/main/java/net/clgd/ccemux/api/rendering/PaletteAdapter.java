package net.clgd.ccemux.api.rendering;

import javax.annotation.Nonnull;

import dan200.computercraft.shared.util.Palette;
import net.clgd.ccemux.api.Utils;

/**
 * Wraps a {@link Palette} object with a given {@link ColorAdapter} to make it
 * easier to get color objects from the palette
 * 
 * @author apemanzilla
 *
 * @param <C>
 */
public final class PaletteAdapter<C> {

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
		 * Creates a color object from the given RGB values, doubles on the range [0, 1]
		 * 
		 * @param r
		 * @param g
		 * @param b
		 * @return
		 */
		T rgb(double r, double g, double b);
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

	public PaletteAdapter(@Nonnull Palette palette, @Nonnull ColorAdapter<C> adapter) {
		this.palette = palette;
		this.adapter = adapter;
	}

	@Nonnull
	public Palette getPalette() {
		return this.palette;
	}

	@Nonnull
	public ColorAdapter<C> getAdapter() {
		return this.adapter;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PaletteAdapter other = (PaletteAdapter) obj;
		if (adapter == null) {
			if (other.adapter != null) return false;
		} else if (!adapter.equals(other.adapter)) return false;
		if (palette == null) {
			if (other.palette != null) return false;
		} else if (!palette.equals(other.palette)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adapter == null) ? 0 : adapter.hashCode());
		result = prime * result + ((palette == null) ? 0 : palette.hashCode());
		return result;
	}

	@Override
	public java.lang.String toString() {
		return "PaletteAdapter(palette=" + this.getPalette() + ", adapter=" + this.getAdapter() + ")";
	}
}
