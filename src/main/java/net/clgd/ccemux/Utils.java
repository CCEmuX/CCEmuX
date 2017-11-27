package net.clgd.ccemux;

import static com.google.common.primitives.Doubles.constrainToRange;

import java.awt.Color;
import java.lang.Character.UnicodeBlock;

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

	/**
	 * Checks if a character is printable
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isPrintableChar(char c) {
		UnicodeBlock block = UnicodeBlock.of(c);
		return !Character.isISOControl(c) && block != null && block != UnicodeBlock.SPECIALS;
	}
}
