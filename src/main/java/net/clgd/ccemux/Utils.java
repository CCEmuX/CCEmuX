package net.clgd.ccemux;

import static com.google.common.primitives.Doubles.constrainToRange;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

import dan200.computercraft.shared.util.Colour;

public class Utils {

	public static final String BASE_16 = "0123456789abcdef";

	public static int base16ToInt(char c) {
		return BASE_16.indexOf(String.valueOf(c).toLowerCase());
	}

	public static char intToBase16(int p) {
		return BASE_16.charAt(p);
	}

	public static Color getCCColourFromInt(int i) {
		Colour col = Colour.fromInt(15 - i);
		return col == null ? Color.WHITE : new Color(col.getR(), col.getG(), col.getB());
	}

	public static Color getCCColourFromChar(char c) {
		return getCCColourFromInt(base16ToInt(c));
	}

	public static double[] clampColor(double[] col) {
		return new double[] { constrainToRange(col[0], 0, 1), constrainToRange(col[1], 0, 1),
				constrainToRange(col[2], 0, 1) };
	}

	public static BufferedImage makeTintedCopy(BufferedImage bi, Color tint) {
		BufferedImage tinted;
		if (!GraphicsEnvironment.isHeadless()) {
			GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();

			tinted = gc.createCompatibleImage(bi.getWidth(), bi.getHeight(), Transparency.TRANSLUCENT);
		} else {
			tinted = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		}

		for (int y = 0; y < bi.getHeight(); y++) {
			for (int x = 0; x < bi.getWidth(); x++) {
				int rgb = bi.getRGB(x, y);

				if (rgb != 0) tinted.setRGB(x, y, tint.getRGB());
			}
		}

		return tinted;
	}

	/**
	 * Tries to get a value as the result of a {@link Callable}, and if an
	 * exception occurs, instead returns a given value.
	 * 
	 * @param getter
	 *            A <code>Callable</code> that may produce a value or throw an
	 *            exception
	 * @param other
	 *            The value to use it the <code>Callable</code> throws an
	 *            <code>Exception</code>
	 * @return The value returned by the <code>Callable</code>, or the second
	 *         parameter if the <code>Callable</code> threw an
	 *         <code>Exception</code>
	 * @see #tryGet(Callable)
	 */
	public static <T> T tryGet(Callable<T> getter, T other) {
		T got;
		try {
			got = getter.call();
		} catch (Exception e) {
			got = other;
		}

		return got;
	}

	/**
	 * Returns the value returned by a given {@link Callable}, or returns
	 * <code>null</code> if the <code>Callable</code> throws an
	 * <code>Exception</code>.
	 * 
	 * @param getter
	 *            A <code>Callable</code> that may produce a value or throw an
	 *            <code>Exception</code>
	 * @return The value returned by the <code>Callable</code>, or
	 *         <code>null</code> if the <code>Callable</code> threw an
	 *         <code>Exception</code>
	 * @see #tryGet(Callable, Object)
	 */
	public static <T> T tryGet(Callable<T> getter) {
		return tryGet(getter, null);
	}
}
