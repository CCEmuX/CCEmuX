package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import dan200.computercraft.core.terminal.Terminal;
import lombok.Getter;
import lombok.Setter;
import net.clgd.ccemux.emulation.CCEmuX;
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

@Getter
public class GDXAdapter extends ApplicationAdapter implements Renderer {
	public static final String EMU_WINDOW_TITLE = "CCEmuX";
	
	private static boolean isPrintableChar(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return !Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null
			&& block != Character.UnicodeBlock.SPECIALS;
	}
	
	private final GDXPlugin plugin;
	private final EmulatedComputer computer;
	private final Terminal terminal;
	private final EmuConfig config;
	
	private volatile Thread thread;
	
	@Setter private volatile boolean focused;
	@Setter @Nullable private Lwjgl3Window window; // TODO: can be null if main window
	
	private volatile InputMultiplexer inputMultiplexer;
	
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	private TerminalRenderer terminalRenderer;
	
	private int pixelWidth, pixelHeight;
	private int margin;
	private int screenWidth, screenHeight;
	
	private int dragButton = 4;
	private Vector2 lastDragSpot = null;
	
	private double blinkLockedTime = 0D;
	
	private final List<Listener> listeners = new ArrayList<>();
	
	GDXAdapter(GDXPlugin plugin, EmulatedComputer computer, EmuConfig config) {
		this.plugin = plugin;
		this.computer = computer;
		this.terminal = computer.terminal;
		this.config = config;
		
		computer.terminal.getEmulatedPalette().addListener((i, r, g, b) -> {
			if (terminalRenderer != null) terminalRenderer.updatePalette(i, r, g, b);
		});
		
		initialisePixelSize(config);
	}
	
	private void initialisePixelSize(EmuConfig config) {
		double termScale = config.termScale.get();
		
		pixelWidth = (int) (6 * termScale);
		pixelHeight = (int) (9 * termScale);
		margin = (int) (2 * termScale);
		
		screenWidth = (terminal.getWidth() * pixelWidth) + (margin * 2);
		screenHeight = (terminal.getHeight() * pixelHeight) + (margin * 2);
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
		
		terminalRenderer = new TerminalRenderer(this, terminal, config);
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
		terminalRenderer.resize(width, height);
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
		blinkLockedTime = Math.max(0, blinkLockedTime - dt);
		
		if (terminalRenderer != null) terminalRenderer.blinkLocked = blinkLockedTime > 0;
	}
}
