package net.clgd.ccemux.rendering;

import java.awt.Color;

import dan200.computercraft.shared.util.Palette;
import net.clgd.ccemux.Utils;

public class PaletteCacher {
	private Palette currentPalette;

	public PaletteCacher(Palette p) {
		currentPalette = clonePalette(p);
	}

	public void setCurrentPalette(Palette p) {
		currentPalette = clonePalette(p);
	}

	private Palette clonePalette(Palette from) {
		return copyPalette(from, new Palette());
	}

	private Palette copyPalette(Palette from, Palette to) {
		for (int i = 0; i < 16; i++) {
			double[] col = from.getColour(i);
			to.setColour(i, col[0], col[1], col[2]);
		}
		return to;
	}

	public Color getColor(char c) {
		return getColor(Utils.base16ToInt(c));
	}

	public Color getColor(int c) {
		double[] col;
		if ((col = currentPalette.getColour(15 - c)) == null) {
			col = currentPalette.getColour(0);
		}
		col = Utils.clampColor(col);
		return new Color((float)col[0], (float)col[1], (float)col[2]);
	}
}
