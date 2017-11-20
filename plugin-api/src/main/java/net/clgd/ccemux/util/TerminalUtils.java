package net.clgd.ccemux.util;

import static com.google.common.primitives.Doubles.constrainToRange;

import java.lang.Character.UnicodeBlock;

import lombok.experimental.UtilityClass;

/**
 * A utility class with some basic rendering utilities
 */
@UtilityClass
public class TerminalUtils {
	/**
	 * Converts a hex char to an integer
	 * 
	 * @param c
	 *            The hexadecimal character
	 * @return The integer represented by the given hex character
	 */
	public static int fromHexChar(char c) {
		return Integer.parseInt("" + c, 16);
	}

	/**
	 * Converts an integer to a hex char
	 * 
	 * @param p
	 *            The integer
	 * @return The hex character representing the given integer
	 */
	public static char toHexChar(int p) {
		return Integer.toHexString(p).charAt(0);
	}

	/**
	 * Clamps a given set of RGB values to the range [0, 1]
	 * 
	 * @param col
	 *            A double[3]
	 * @return A new double[3] with the given values constrained to the range
	 *         [0, 1]
	 */
	public static double[] clampColor(double[] col) {
		return new double[] { constrainToRange(col[0], 0, 1), constrainToRange(col[1], 0, 1),
				constrainToRange(col[2], 0, 1) };
	}

	/**
	 * Checks whether a given character is printable
	 * 
	 * @param c
	 *            A character
	 * @return Whether the given character is printable
	 */
	public static boolean isPrintableChar(char c) {
		UnicodeBlock block = UnicodeBlock.of(c);
		return !Character.isISOControl(c) && block != null && block != UnicodeBlock.SPECIALS;
	}
}
