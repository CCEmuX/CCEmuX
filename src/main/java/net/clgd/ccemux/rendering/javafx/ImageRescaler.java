package net.clgd.ccemux.rendering.javafx;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ImageRescaler {
	/**
	 * Creates a scaled copy of the given image using nearest-neighbor scaling
	 * 
	 * @param base
	 *            The base image
	 * @param hscale
	 *            The horizontal scale value
	 * @param vscale
	 *            The vertical scale value
	 * @return
	 */
	public static Image rescale(Image base, double hscale, double vscale) {
		int w = (int) Math.round(base.getWidth() * hscale);
		int h = (int) Math.round(base.getHeight() * vscale);

		val out = new WritableImage(w, h);

		val reader = base.getPixelReader();
		val writer = out.getPixelWriter();

		int nx, ny;

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				nx = (int) Math.floor(x / hscale);
				ny = (int) Math.floor(y / vscale);
				writer.setArgb(x, y, reader.getArgb(nx, ny));
			}
		}

		return out;
	}
}
