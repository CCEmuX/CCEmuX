package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.rendering.TerminalFont;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
public class GDXTerminalFont extends TerminalFont {
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
		InputStream is = url.openStream();
		Gdx2DPixmap gdx2DPixmap = new Gdx2DPixmap(is, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888);
		texture = new Texture(new Pixmap(gdx2DPixmap));
	}
}
