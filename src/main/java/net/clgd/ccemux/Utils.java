package net.clgd.ccemux;

import dan200.computercraft.shared.util.Colour;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Utils {

	public static final String BASE_16 = "0123456789abcdef";

	public static int base16ToInt(char c) {
		return BASE_16.indexOf(String.valueOf(c).toLowerCase());
	}

	public static Color getCCColourFromInt(int i) {
		Colour col = Colour.fromInt(15 - i);
		return col == null ? Color.WHITE : new Color(col.getR(), col.getG(), col.getB());
	}

	public static Color getCCColourFromChar(char c) {
		return getCCColourFromInt(base16ToInt(c));
	}

	public static double[] clampColor(double[] col) {
		return new double[]{clamp(col[0], 0f, 1f), clamp(col[1], 0f, 1f), clamp(col[2], 0f, 1f)};
	}

	public static double clamp(double f, double min, double max) {
		if (f > max) {
			return max;
		} else if (f < min) {
			return min;
		} else {
			return f;
		}
	}

	public static BufferedImage makeTintedCopy(BufferedImage bi, Color tint) {
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();

		BufferedImage tinted = gc.createCompatibleImage(bi.getWidth(), bi.getHeight(), Transparency.TRANSLUCENT);

		for (int y = 0; y < bi.getHeight(); y++) {
			for (int x = 0; x < bi.getWidth(); x++) {
				int rgb = bi.getRGB(x, y);

				if (rgb != 0)
					tinted.setRGB(x, y, tint.getRGB());
			}
		}

		return tinted;
	}
}
