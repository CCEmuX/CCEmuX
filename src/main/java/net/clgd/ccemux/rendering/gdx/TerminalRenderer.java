package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.util.Palette;
import net.clgd.ccemux.Utils;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TerminalRenderer {
	private static final int COLOUR_COUNT = 16;
	
	private final GDXPlugin plugin;
	private final GDXAdapter adapter;
	private final EmulatedComputer computer;
	private final Terminal terminal;
	
	private GDXTerminalFont font;
	private Texture fontTexture;
	private float fontTexWidth, fontTexHeight;
	private Map<Integer, Color> fontColours = new HashMap<>();
	private Texture backgroundTexture;
	
	private int pixelWidth, pixelHeight;
	private int margin;
	private int screenWidth, screenHeight;
	
	public char cursorChar = '_';
	
	public boolean blinkLocked = false;
	
	public TerminalRenderer(GDXAdapter adapter, Terminal terminal, EmuConfig config) {
		this.plugin = adapter.getPlugin();
		this.adapter = adapter;
		this.computer = adapter.getComputer();
		this.terminal = terminal;
		
		this.pixelWidth = adapter.getPixelWidth();
		this.pixelHeight = adapter.getPixelHeight();
		this.margin = adapter.getMargin();
		this.screenWidth = adapter.getScreenWidth();
		this.screenHeight = adapter.getScreenHeight();
		
		initialise(config);
	}
	
	private void initialise(EmuConfig config) {
		initialiseFont();
		initialisePalette();
		initialiseBackgroundTexture();
	}
	
	private void initialiseFont() {
		font = GDXTerminalFont.getBestFont();
		fontTexture = font.getTexture();
		fontTexWidth = fontTexture.getWidth();
		fontTexHeight = fontTexture.getHeight();
	}
	
	private void initialisePalette() {
		Palette p = terminal.getPalette();
		
		for (int i = 0; i < COLOUR_COUNT; i++) {
			double[] c = p.getColour(COLOUR_COUNT - 1 - i);
			fontColours.put(i, new Color((float) c[0], (float) c[1], (float) c[2], 1f));
		}
	}
	
	void updatePalette(int i, double r, double g, double b) {
		fontColours.put(COLOUR_COUNT - 1 - i, new Color((float) r, (float) g, (float) b, 1f));
	}
	
	private void initialiseBackgroundTexture() {
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(1f, 1f, 1f, 1f);
		pixmap.fill();
		backgroundTexture = new Texture(pixmap);
	}
	
	void render(SpriteBatch batch) {
		drawTerminal(batch);
	}
	
	private void drawChar(SpriteBatch batch, char c, int x, int y, int colour) {
		if ((int) c == 0) return;
		
		Rectangle r = font.getCharCoords(c);
		
		batch.setColor(fontColours.get(colour));
		batch.draw(
			fontTexture,
			// dest
			x, screenHeight - y, pixelWidth, -pixelHeight,
			// source
			r.x / fontTexWidth, r.y / fontTexHeight,
			(r.x + r.width) / fontTexWidth, (r.y + r.height) / fontTexHeight
		);
	}
	
	private void drawBackground(SpriteBatch batch, float x, float y, int width, int height, int colour) {
		batch.setColor(fontColours.get(colour));
		batch.draw(
			backgroundTexture,
			x, screenHeight - y, width, -height
		);
	}
	
	private void drawTerminal(SpriteBatch batch) {
		synchronized (terminal) {
			float dx = 0, dy = 0;
			
			for (int y = 0; y < terminal.getHeight(); y++) {
				TextBuffer textLine = terminal.getLine(y);
				TextBuffer bgLine = terminal.getBackgroundColourLine(y);
				TextBuffer fgLine = terminal.getTextColourLine(y);
				
				int height = (y == 0 || y == terminal.getHeight() - 1) ? pixelHeight + margin : pixelHeight;
				
				for (int x = 0; x < terminal.getWidth(); x++) {
					int width = (x == 0 || x == terminal.getWidth() - 1) ? pixelWidth + margin : pixelWidth;
				
					drawBackground(
						batch,
						dx, dy,
						width, height,
						Utils.base16ToInt((bgLine == null) ? 'f' : bgLine.charAt(x))
					);
					
					char character = (textLine == null) ? ' ' : textLine.charAt(x);
					char fgChar = (fgLine == null) ? ' ' : fgLine.charAt(x);
					
					drawChar(
						batch,
						character,
						x * pixelWidth + margin, y * pixelHeight + margin,
						Utils.base16ToInt(fgChar)
					);
					
					dx += width;
				}
				
				dx = 0;
				dy += height;
			}
			
			
			boolean blink = terminal.getCursorBlink() && (blinkLocked || CCEmuX.getGlobalCursorBlink());
			
			if (blink) {
				drawChar(
					batch,
					cursorChar,
					terminal.getCursorX() * pixelWidth + margin,
					terminal.getCursorY() * pixelHeight + margin,
					terminal.getTextColour()
				);
			}
		}
	}
	
	void resize(int width, int height) {
		screenWidth = width;
		screenHeight = height;
	}
}
