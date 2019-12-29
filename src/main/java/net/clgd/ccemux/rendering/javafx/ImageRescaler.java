package net.clgd.ccemux.rendering.javafx;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ImageRescaler {
	/**
	 * Creates a scaled copy of the given image using nearest-neighbor scaling
	 *
	 * @param base   The base image
	 * @param hscale The horizontal scale value
	 * @param vscale The vertical scale value
	 * @return The rescaled image
	 */
	public static Image rescale(Image base, double hscale, double vscale) {
		int w = (int) Math.round(base.getWidth() * hscale);
		int h = (int) Math.round(base.getHeight() * vscale);

		WritableImage out = new WritableImage(w, h);

		PixelReader reader = base.getPixelReader();
		PixelWriter writer = out.getPixelWriter();

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
