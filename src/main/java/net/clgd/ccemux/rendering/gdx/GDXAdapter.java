package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import dan200.computercraft.core.terminal.Terminal;
import lombok.Getter;
import lombok.Setter;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.TerminalFont;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Getter
public class GDXAdapter extends ApplicationAdapter implements Renderer {
	public static final String EMU_WINDOW_TITLE = "CCEmuX";
	
	private static final double ACTION_TIME = 0.5;
	
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
	
	@Setter private double blinkLockedTime = 0D;
	
	@Setter private double terminateTimer = -1;
	@Setter private double shutdownTimer = -1;
	@Setter private double rebootTimer = -1;
	
	private String title;
	
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
	
	private String getWindowTitle() {
		int id = computer.getID();
		String title = EMU_WINDOW_TITLE + " - ";
		
		if (computer.getLabel() != null) {
			title += computer.getLabel() + " (Computer #" + id + ")";
		} else {
			title += "Computer #" + id;
		}
		
		return title;
	}
	
	@Override
	public void onAdvance(double dt) {
		if (Gdx.graphics == null) return;
		
		if (getWindowTitle() != title) {
			title = getWindowTitle();
			
			if (window != null) {
				window.setTitle(title);
			} else {
				Gdx.graphics.setTitle(title);
			}
		}
		
		handleBlinkLock(dt);
		handleActionKeys(dt);
	}
	
	private void handleBlinkLock(double dt) {
		blinkLockedTime = Math.max(0, blinkLockedTime - dt);
		
		if (terminalRenderer != null) terminalRenderer.blinkLocked = blinkLockedTime > 0;
	}
	
	@SuppressWarnings("Duplicates")
	private void handleActionKeys(double dt) {
		if (shutdownTimer >= 0 && shutdownTimer < ACTION_TIME) {
			shutdownTimer += dt;
			
			if (shutdownTimer >= ACTION_TIME) computer.shutdown();
		}
		
		if (rebootTimer >= 0 && rebootTimer < ACTION_TIME) {
			rebootTimer += dt;
			
			if (rebootTimer >= ACTION_TIME) {
				if (computer.isOn()) {
					computer.reboot();
				} else {
					computer.turnOn();
				}
			}
		}
		
		if (terminateTimer >= 0 && terminateTimer < ACTION_TIME) {
			terminateTimer += dt;
			
			if (terminateTimer >= ACTION_TIME) computer.terminate();
		}
	}
	
	/**
	 * Determine whether {@code key} and {@code char} events should be queued.
	 *
	 * If any of the action keys are pressed (terminate, shutdown, reboot) then such events will
	 * be blocked.
	 *
	 * @return Whether such events should be queued.
	 */
	boolean allowKeyEvents() {
		return shutdownTimer < 0 && rebootTimer < 0 && terminateTimer < 0;
	}
}
