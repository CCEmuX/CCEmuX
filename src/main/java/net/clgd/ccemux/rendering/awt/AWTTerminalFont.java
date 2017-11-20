package net.clgd.ccemux.rendering.awt;

import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.rendering.TerminalFont;
import net.clgd.ccemux.util.TerminalUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
public class AWTTerminalFont extends TerminalFont {
	private final BufferedImage base;

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

	public BufferedImage getBitmap() {
		return base;
	}
}
