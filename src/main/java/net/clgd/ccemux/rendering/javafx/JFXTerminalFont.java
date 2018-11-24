package net.clgd.ccemux.rendering.javafx;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.val;
import net.clgd.ccemux.api.rendering.TerminalFont;

@Value
@EqualsAndHashCode(callSuper = false)
public class JFXTerminalFont extends TerminalFont {
	@Getter(lazy = true)
	private static final JFXTerminalFont bestFont = TerminalFont.getBest(JFXTerminalFont::new);

	private final Image base;

	@Value
	public static class CharImageRequest {
		private char character;
		private Color color;
		private double termScale;
	}

	private final Cache<CharImageRequest, Image> charCache = CacheBuilder.newBuilder().expireAfterAccess(30, SECONDS)
			.maximumSize(1000).initialCapacity(200).build();

	public JFXTerminalFont(Image base) {
		super(base.widthProperty().intValue(), base.heightProperty().intValue());
		this.base = base;
	}

	public JFXTerminalFont(String url) {
		this(new Image(url));
	}

	public JFXTerminalFont(URL url) {
		this(url.toString());
	}

	public Image generateCharImage(char c, Color color) {
		val coords = getCharCoords(c);
		val out = new WritableImage(coords.width, coords.height);

		val reader = base.getPixelReader();
		val writer = out.getPixelWriter();

		// TODO: probably faster to use a PixelFormat
		for (int x = 0; x < coords.width; x++) {
			for (int y = 0; y < coords.height; y++) {
				Color oc = reader.getColor(coords.x + x, coords.y + y);

				writer.setColor(x, y, Color.color(oc.getRed() * color.getRed(), oc.getGreen() * color.getGreen(),
						oc.getBlue() * color.getBlue(), oc.getOpacity()));
			}
		}

		return out;
	}

	public Image generateScaledCharImage(char c, Color color, double termScale) {
		val base = generateCharImage(c, color);

		return ImageRescaler.rescale(base, termScale / this.getHorizontalScale(), termScale / this.getVerticalScale());
	}

	public Image getCharImage(char c, Color color, double termScale) {
		try {
			return charCache.get(new CharImageRequest(c, color, termScale),
				() -> generateScaledCharImage(c, color, termScale));
		} catch (ExecutionException e) {
			Throwable inner = e.getCause();
			if (inner instanceof RuntimeException) throw (RuntimeException) inner;
			if (inner instanceof Error) throw (Error) inner;
			throw new RuntimeException(e);
		}
	}
}
