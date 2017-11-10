package net.clgd.ccemux.rendering.awt;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.Utils;
import net.clgd.ccemux.rendering.TerminalFont;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AWTTerminalFont extends TerminalFont {
	private final BufferedImage base;

	private final Cache<Color, BufferedImage> tintedImages = CacheBuilder.newBuilder()
			.initialCapacity(16)
			.maximumSize(64)
			.expireAfterAccess(30, TimeUnit.SECONDS)
			.build();

	public AWTTerminalFont(BufferedImage base) {
		super(base.getWidth(), base.getHeight());
		this.base = base;
	}

	public AWTTerminalFont(InputStream stream) throws IOException {
		this(ImageIO.read(stream));
	}

	public AWTTerminalFont(URL url) throws IOException {
		this(ImageIO.read(url));
	}

	public BufferedImage getTintedBitmap(Color colour) {
		try {
			return tintedImages.get(colour, () -> Utils.makeTintedCopy(base, colour));
		} catch (ExecutionException e) {
			log.error("Failed to get tinted font for colour {}", colour, e);
			throw new RuntimeException(e);
		}
	}
}
