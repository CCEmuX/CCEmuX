package net.clgd.ccemux.api;

import java.lang.Character.UnicodeBlock;

import javax.annotation.Nonnull;

/**
 * A set of general-purpose CC-related utilities
 */
public final class Utils {
	/**
	 * Gets the global cursor blink
	 *
	 * @return Whether a blinking cursor should be shown right now
	 */
	public static boolean getGlobalCursorBlink() {
		return System.currentTimeMillis() / 400 % 2 == 0;
	}

	private static final String BASE_16 = "0123456789abcdef";

	/**
	 * Converts a single hexadecimal character to an int
	 *
	 * @param c The hexadecimal character
	 * @return The associated int, or -1 if the character is invalid
	 */
	public static int base16ToInt(char c) {
		return BASE_16.indexOf(String.valueOf(c).toLowerCase());
	}

	/**
	 * Converts an int to matching hexadecimal character
	 *
	 * @param p An integer on the range [0, 15]
	 * @return The matching hexadecimal character
	 * @throws IndexOutOfBoundsException if the integer is not on the range [0, 15]
	 */
	public static char intToBase16(int p) {
		return BASE_16.charAt(p);
	}

	/**
	 * Constrains the given decimal value to a given range
	 *
	 * @param val The value
	 * @param min The bottom bound of the range
	 * @param max The upper bound of the range
	 * @return The value, constrained to the range [min, max]
	 */
	public static double constrainToRange(double val, double min, double max) {
		return Math.max(min, Math.min(max, val));
	}

	/**
	 * Clamps a set of three doubles (RGB values) to the range [0, 1]
	 *
	 * @param col The three values
	 * @return A new array with the constrained values
	 */
	@Nonnull
	public static double[] clampColor(@Nonnull double[] col) {
		return new double[] { constrainToRange(col[0], 0, 1), constrainToRange(col[1], 0, 1), constrainToRange(col[2], 0, 1) };
	}

	/**
	 * Checks if a character is printable
	 *
	 * @param c The character
	 * @return Whether the character is printable
	 */
	public static boolean isPrintableChar(char c) {
		UnicodeBlock block = UnicodeBlock.of(c);
		return !Character.isISOControl(c) && block != null && block != UnicodeBlock.SPECIALS;
	}

	private Utils() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
