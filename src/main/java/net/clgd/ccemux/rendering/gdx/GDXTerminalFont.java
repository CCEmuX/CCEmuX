package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.rendering.TerminalFont;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
public class GDXTerminalFont extends TerminalFont {
	@Getter(lazy = true)
	private static final GDXTerminalFont bestFont = TerminalFont.getBest(GDXTerminalFont::new);
	
	@Getter private Texture texture;
	
	public GDXTerminalFont(URL url) {
		try {
			loadTexture(url);
		} catch (IOException e) {
			log.error("Failed to load font {}", url);
			throw new RuntimeException(e);
		}
		
		calculateCharSizes(texture.getWidth(), texture.getHeight());
	}
	
	private void loadTexture(URL url) throws IOException {
		try (InputStream is = url.openStream()) {
			byte[] b = IOUtils.toByteArray(is);
			texture = new Texture(new Pixmap(b, 0, b.length));
		}
	}
}
