package net.clgd.ccemux.rendering.javafx;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lombok.*;
import net.clgd.ccemux.rendering.TerminalFont;

@Value
@EqualsAndHashCode(callSuper = false)
public class JFXTerminalFont extends TerminalFont {
	@Getter(lazy = true)
	private static final JFXTerminalFont bestFont = TerminalFont.getBest(JFXTerminalFont::new);

	private final Image base;

	private final Cache<Pair<Character, Color>, Image> charCache = CacheBuilder.newBuilder()
			.expireAfterAccess(10, SECONDS).maximumSize(1000).initialCapacity(200).build();

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
				int argb = reader.getArgb(x, y);

				// TODO implement colorizing
				// int alpha = (argb >> 24) & 0xFF;
				// int red = (argb >> 16) & 0xFF;
				// int green = (argb >> 8) & 0xFF;
				// int blue = (argb) & 0xFF;

				writer.setArgb(x, y, argb);
			}
		}

		return out;
	}

	@SneakyThrows(ExecutionException.class)
	public Image getCharImage(char c, Color color) {
		return charCache.get(Pair.of(c, color), () -> generateCharImage(c, color));
	}
}
