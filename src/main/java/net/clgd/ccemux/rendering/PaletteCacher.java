package net.clgd.ccemux.rendering;

import dan200.computercraft.shared.util.Palette;
import net.clgd.ccemux.Utils;

import java.awt.*;

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
			float[] col = from.getColour(i);
			to.setColour(i, col[0], col[1], col[2]);
		}
		return to;
	}

	public Color getColor(char c) {
		return getColor(Utils.base16ToInt(c));
	}

	public Color getColor(int c) {
		float[] col = Utils.clampColor(currentPalette.getColour(15 - c));
		return new Color(col[0], col[1], col[2]);
	}
}
