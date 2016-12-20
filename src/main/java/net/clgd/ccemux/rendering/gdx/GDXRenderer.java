package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.google.common.io.ByteStreams;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.util.Colour;
import net.clgd.ccemux.Utils;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.Renderer;
import org.lwjgl.opengl.Display;

import java.io.IOException;

public class GDXRenderer extends ApplicationAdapter implements Renderer, InputProcessor {
	private static final String CC_FONT_PATH = "/assets/computercraft/textures/gui/termFont.png";

	private static final int FONT_CHAR_WIDTH = 6;
	private static final int FONT_CHAR_HEIGHT = 9;

	private static final int FONT_MAP_WIDTH = 96;
	private static final int FONT_MAP_HEIGHT = 144;

	private final EmulatedComputer computer;
	private final Terminal terminal;

	private final int pixelWidth;
	private final int pixelHeight;
	private final int margin;

	private int dragButton = 4;

	private boolean blinkLocked = false;
	private double blinkLockedTime = 0d;

	private ShapeRenderer bgBatch;
	private SpriteBatch fgBatch;

	private TextureRegion[] fontChars;

	public GDXRenderer(EmulatedComputer computer) {
		this.computer = computer;
		this.terminal = computer.terminal;

		int termScale = computer.emu.conf.getTermScale();
		pixelWidth = 6 * termScale;
		pixelHeight = 9 * termScale;
		margin = 2 * termScale;

		initialiseWindow();
	}

	private void initialiseWindow() {
		Vector2 windowSize = getWindowSize(terminal.getWidth(), terminal.getHeight());

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = (int) windowSize.x;
		config.height = (int) windowSize.y;
		config.forceExit = true;
		config.resizable = false;

		new LwjglApplication(this, config);
	}

	private Vector2 getWindowSize(int terminalWidth, int terminalHeight) {
		int screenWidth = terminalWidth * pixelWidth + margin * 2;
		int screenHeight = terminalHeight * pixelHeight + margin * 2;

		return new Vector2(screenWidth, screenHeight);
	}

	// same as Utils.getCCColourFromInt, but uses GDX colours
	private static Color getCCColourFromInt(int i) {
		Colour col = Colour.fromInt(15 - i);
		return col == null ? Color.WHITE : new Color(col.getR(), col.getG(), col.getB(), 1.0f);
	}

	private static Color getCCColourFromChar(char c) {
		return getCCColourFromInt(Utils.base16ToInt(c));
	}

	private Vector2 mapPointToCC(Vector2 p) {
		int px = (int) p.x - margin;
		int py = (int) p.y - margin;

		int x = px / pixelWidth;
		int y = py / pixelHeight;

		return new Vector2(x + 1, y + 1);
	}

	@Override
	public void create() {
		super.create();

		bgBatch = new ShapeRenderer();
		fgBatch = new SpriteBatch();

		try {
			byte[] fontBytes = ByteStreams.toByteArray(ComputerCraft.class.getResourceAsStream(CC_FONT_PATH));
			Pixmap fontPixmap = new Pixmap(fontBytes, 0, fontBytes.length);
			Texture font = new Texture(fontPixmap);

			int fontColumns = FONT_MAP_WIDTH / FONT_CHAR_WIDTH;
			int fontRows = FONT_MAP_HEIGHT / FONT_CHAR_HEIGHT;

			fontChars = new TextureRegion[fontColumns * fontRows];

			for (int i = 0; i < fontChars.length; i++) {
				int x = i % fontColumns;
				int y = i / fontColumns;

				fontChars[i] = new TextureRegion(
					font,
					x * FONT_CHAR_WIDTH,
					y * FONT_CHAR_HEIGHT,
					FONT_CHAR_WIDTH,
					FONT_CHAR_HEIGHT
				);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render() {
		super.render();

		float dt = Gdx.graphics.getDeltaTime();

		blinkLockedTime = Math.max(0, blinkLockedTime - dt);
		blinkLocked = blinkLockedTime > 0;

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		bgBatch.begin(ShapeRenderer.ShapeType.Filled);
		drawBG();
		bgBatch.end();

		fgBatch.begin();
		drawFG();
		drawCursor();
		fgBatch.end();
	}

	private void drawBG() {
		int dx = 0;
		int dy = 0;

		for (int y = terminal.getHeight() - 1; y >= 0; y--) {
			TextBuffer bgLine = terminal.getBackgroundColourLine(y);

			int height = (y == 0 || y == terminal.getHeight() - 1) ? pixelHeight + margin : pixelHeight;

			for (int x = 0; x < terminal.getWidth(); x++) {
				int width = (x == 0 || x == terminal.getWidth() - 1) ? pixelWidth + margin : pixelWidth;

				drawPixel(
					dx, dy,
					width, height,
					getCCColourFromChar((bgLine == null) ? 'f' : bgLine.charAt(x))
				);

				dx += width;
			}

			dx = 0;
			dy += height;
		}
	}

	private void drawFG() {
		for (int y = terminal.getHeight() - 1; y >= 0; y--) {
			TextBuffer textLine = terminal.getLine(y);
			TextBuffer fgLine = terminal.getTextColourLine(y);

			for (int x = 0; x < terminal.getWidth(); x++) {
				char character = (textLine == null) ? ' ' : textLine.charAt(x);
				char fgChar = (fgLine == null) ? ' ' : fgLine.charAt(x);

				drawChar(
					character,
					x * pixelWidth + margin, (terminal.getHeight() - y - 1) * pixelHeight + margin,
					Utils.base16ToInt(fgChar)
				);
			}
		}
	}

	private void drawCursor() {
		if (terminal.getCursorBlink() && (blinkLocked || CCEmuX.getGlobalCursorBlink())) {
			drawChar(
				computer.cursorChar,
				terminal.getCursorX() * pixelWidth + margin,
				(terminal.getHeight() - terminal.getCursorY() - 1) * pixelHeight + margin,
				terminal.getTextColour()
			);
		}
	}

	private void drawPixel(int x, int y, int width, int height, Color colour) {
		bgBatch.setColor(colour);
		bgBatch.rect(x, y, width, height);
	}

	private void drawChar(char c, int x, int y, int colour) {
		if ((int) c == 0)
			return;

		fgBatch.setColor(getCCColourFromInt(colour));
		fgBatch.draw(fontChars[c], x, y, pixelWidth, pixelHeight);
	}

	private static boolean isPrintableChar(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);

		return !Character.isISOControl(c) &&
			c != Input.Keys.UNKNOWN &&
			block != null &&
			block != Character.UnicodeBlock.SPECIALS;
	}

	@Override
	public boolean isVisible() {
		return Display.isActive();
	}

	@Override
	public void setVisible(boolean visible) {
		if (!visible) {
			Gdx.app.exit();
		}
	}

	@Override
	public void onTerminalResized(int width, int height) {

	}

	@Override
	public void onAdvance(double dt) {

	}

	private boolean handleCtrlPress(char control) {
		if (control == 't') {
			computer.terminate();
		} else if (control == 'r') {
			if (!computer.isOn()) {
				computer.turnOn();
			} else {
				computer.reboot();
			}
		} else if (control == 's') {
			computer.shutdown();
		} else if (control == 'v') {
			computer.paste(Gdx.app.getClipboard().getContents());
		} else {
			return false;
		}

		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		computer.pressKey(KeyTranslator.translateToCC(keycode), false);

		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (
			Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
			Input.Keys.toString(keycode).length() == 1 &&
			handleCtrlPress(Input.Keys.toString(keycode).toLowerCase().charAt(0))
		) {
			return true;
		}

		computer.pressKey(KeyTranslator.translateToCC(keycode), true);
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		if (isPrintableChar(character)) {
			computer.pressChar(character);
			blinkLockedTime = 0.25d;

			return true;
		}

		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector2 p = mapPointToCC(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
		computer.click(MouseTranslator.gdxToCC(button), (int) p.x, (int) p.y, false);

		dragButton = MouseTranslator.gdxToCC(button);

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector2 p = mapPointToCC(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
		computer.click(MouseTranslator.gdxToCC(button), (int) p.x, (int) p.y, true);

		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		Vector2 p = mapPointToCC(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
		computer.drag(dragButton, (int) p.x, (int) p.y);

		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		int scrollDirection = amount > 0 ? 1 : -1;

		Vector2 p = mapPointToCC(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
		computer.scroll(scrollDirection, (int) p.x, (int) p.y);

		return true;
	}

	@Override
	public void dispose() {
		computer.dispose();
	}

	@Override
	public void onDispose() {
		Gdx.app.exit();
	}
}
