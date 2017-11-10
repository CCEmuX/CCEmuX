package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import lombok.Getter;
import lombok.Setter;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.TerminalFont;

import javax.annotation.Nullable;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GDXAdapter extends ApplicationAdapter implements Renderer {
	public static final String EMU_WINDOW_TITLE = "CCEmuX";
	
	private static boolean isPrintableChar(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null
			&& block != Character.UnicodeBlock.SPECIALS;
	}
	
	@Getter private final GDXPlugin plugin;
	@Getter private final EmulatedComputer computer;
	
	@Getter private volatile Thread thread;
	
	@Getter @Setter private volatile boolean focused;
	@Getter @Setter @Nullable private Lwjgl3Window window; // TODO: can be null if main window
	
	@Getter private volatile InputMultiplexer inputMultiplexer;
	
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	private TerminalRenderer terminalRenderer;
	
	private final int pixelWidth;
	private final int pixelHeight;
	
	private boolean lastBlink = false;
	private int dragButton = 4;
	private Vector2 lastDragSpot = null;
	
	private double blinkLockedTime = 0D;
	
	private boolean paletteChanged = false;
	
	private final List<Listener> listeners = new ArrayList<>();
	
	GDXAdapter(GDXPlugin plugin, EmulatedComputer computer, EmuConfig config) {
		this.plugin = plugin;
		
		this.computer = computer;
		computer.terminal.getEmulatedPalette().addListener((i, r, g, b) -> paletteChanged = true);
		
		pixelWidth = (int) (6 * config.termScale.get());
		pixelHeight = (int) (9 * config.termScale.get());
	}
	
	void startInThread() {
		thread = new Thread(() -> plugin.getManager().initialise(this));
		thread.start();
	}
	
	@Override
	public void create() {
		super.create();
		
		inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(new GDXInputProcessor(this));
		
		batch = new SpriteBatch();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		terminalRenderer = new TerminalRenderer();
	}
	
	@Override
	public void render() {
		super.render();
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		batch.setProjectionMatrix(camera.combined);
		
		terminalRenderer.render(batch);
		
		batch.end();
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		camera.setToOrtho(false, width, height);
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (window != null) window.setVisible(visible);
	}
	
	@Override
	public void addListener(Renderer.Listener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(Renderer.Listener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public TerminalFont loadFont(URL url) throws IOException {
		return new GDXTerminalFont(url);
	}
	
	@Override
	public void onAdvance(double dt) {
	
	}
}
