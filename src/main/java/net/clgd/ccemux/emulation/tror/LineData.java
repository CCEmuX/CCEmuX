package net.clgd.ccemux.emulation.tror;

import java.util.Objects;

public class LineData {
	public final String text;
	public final String fg;
	public final String bg;

	public LineData(String text, String fg, String bg) {
		if (text.length() != fg.length() || fg.length() != bg.length())
			throw new IllegalArgumentException("Arguments must be same length");

		this.text = text;
		this.fg = fg;
		this.bg = bg;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;

		LineData l = (LineData) o;
		return Objects.equals(text, l.text) && Objects.equals(fg, l.fg) && Objects.equals(bg, l.bg);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(text, fg, bg);
	}
	
	@Override
	public String toString() {
		return fg + "," + bg + "," + text;
	}
}
