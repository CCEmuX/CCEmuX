package net.clgd.ccemux.rendering;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public abstract class TerminalFont {
	public static final String FONT_RESOURCE_PATH = "assets/computercraft/textures/gui/term_font.png";

	public static final int BASE_WIDTH = 256, BASE_HEIGHT = 256;

	public static final int BASE_CHAR_WIDTH = 6, BASE_CHAR_HEIGHT = 9;
	public static final int BASE_MARGIN = 1;
	public static final int COLUMNS = 16, ROWS = 16;

	/**
	 * The scale of the font, calculated based on base image resolution
	 */
	@Getter
	private double horizontalScale, verticalScale;

	/**
	 * The scaled character dimensions
	 */
	@Getter
	private int charWidth, charHeight;

	/**
	 * The margin around each character
	 */
	@Getter
	private int margin;
	
	private Map<Character, Rectangle> charCoordCache = new HashMap<>();
	
	protected void calculateCharSizes(int imageWidth, int imageHeight) {
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
	 * @param c
	 *            The character
	 * @return The coordinates and dimensions of a given character
	 */
	public Rectangle getCharCoords(char c) {
		if (charCoordCache.containsKey(c)) return charCoordCache.get(c);
		
		int charcode = (int) c;
		
		Rectangle r = new Rectangle(
				margin + charcode % COLUMNS * (getCharWidth() + margin * 2),
				margin + charcode / ROWS * (getCharHeight() + margin * 2),
				getCharWidth(), getCharHeight()
		);
		
		charCoordCache.put(c, r);
		return r;
	}
}
