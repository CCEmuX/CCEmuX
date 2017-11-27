package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dan200.computercraft.core.terminal.Terminal;
import lombok.Getter;
import lombok.Setter;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;
import net.clgd.ccemux.rendering.Renderer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class GDXAdapter extends BaseGDXAdapter implements Renderer {
	public static final String EMU_WINDOW_TITLE = "CCEmuX";
	
	private static final double ACTION_TIME = 0.5;
	
	private final EmulatedComputer computer;
	private final Terminal terminal;
	private final EmuConfig config;
	
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
		super(plugin);
		
		this.computer = computer;
		this.terminal = computer.terminal;
		this.config = config;
		
		computer.terminal.getPalette().addListener((i, r, g, b) -> {
			if (terminalRenderer != null) terminalRenderer.updatePalette(i, r, g, b);
		});
		
		initialisePixelSize(config);
	}
	
	@Override
	void setupConfig(Lwjgl3WindowConfiguration config) {
		super.setupConfig(config);
		
		config.setResizable(false); // TODO
		config.setWindowedMode(screenWidth, screenHeight);
	}
	
	private void initialisePixelSize(EmuConfig config) {
		double termScale = config.termScale.get();
		
		pixelWidth = (int) (6 * termScale);
		pixelHeight = (int) (9 * termScale);
		margin = (int) (2 * termScale);
		
		screenWidth = (terminal.getWidth() * pixelWidth) + (margin * 2);
		screenHeight = (terminal.getHeight() * pixelHeight) + (margin * 2);
	}
	
	@Override
	public void create() {
		super.create();
		
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
		
		if (!getWindowTitle().equals(title)) {
			title = getWindowTitle();
			
			try {
				setTitle(title);
			} catch (NullPointerException ignored) {
				title = ""; // give it another chance to update title
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
	
	@Override
	void filesDropped(String[] files) {
		try {
			computer.copyFiles(Arrays.stream(files).map(File::new).collect(Collectors.toList()), "/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		computer.shutdown();
		listeners.forEach(Listener::onClosed);
		
		batch.dispose();
		terminalRenderer.dispose();
		
		plugin.getManager().removeAdapter(this);
	}
}
