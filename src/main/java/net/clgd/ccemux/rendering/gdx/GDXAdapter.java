package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import lombok.Getter;
import lombok.Setter;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;
import net.clgd.ccemux.rendering.Renderer;

public class GDXAdapter extends ApplicationAdapter implements Renderer {
	@Getter private final GDXPlugin plugin;
	@Getter private final EmulatedComputer computer;
	
	@Getter private volatile Thread thread;
	
	@Getter @Setter private volatile boolean focused;
	@Getter @Setter private volatile Lwjgl3Window window; // TODO: can be null if main window
	
	@Getter private volatile InputMultiplexer inputMultiplexer;
	
	private SpriteBatch batch;
	private BitmapFont font;
	private OrthographicCamera camera;
	
	GDXAdapter(GDXPlugin plugin, EmulatedComputer computer, EmuConfig cfg) {
		this.plugin = plugin;
		this.computer = computer;
	}
	
	void startInThread() {
		thread = new Thread(() -> plugin.getManager().initialise(this));
		thread.start();
	}
	
	@Override
	public void create() {
		super.create();
		
		inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(new InputProcessor() {
			@Override
			public boolean keyDown(int keycode) {
				return false;
			}
			
			@Override
			public boolean keyUp(int keycode) {
				return false;
			}
			
			@Override
			public boolean keyTyped(char character) {
				if (character == 'a') {
					plugin.getManager().createWindow(computer, null);
					return true;
				}
				
				return false;
			}
			
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				return false;
			}
			
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				return false;
			}
			
			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				return false;
			}
			
			@Override
			public boolean mouseMoved(int screenX, int screenY) {
				return false;
			}
			
			@Override
			public boolean scrolled(int amount) {
				return false;
			}
		});
		
		batch = new SpriteBatch();
		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Lato-Regular.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 16;
		font = generator.generateFont(parameter);
		font.getData().markupEnabled = true;
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	@Override
	public void render() {
		super.render();
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		batch.setProjectionMatrix(camera.combined);
		
		font.draw(batch, focused ? "[GREEN]focused[]" : "[RED]unfocused[]", 200, 200);
		
		batch.end();
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		camera.setToOrtho(false, width, height);
	}
	
	@Override
	public boolean isVisible() {
		return false;
	}
	
	@Override
	public void setVisible(boolean visible) {
	
	}
	
	@Override
	public void addListener(Listener l) {
	
	}
	
	@Override
	public void removeListener(Listener l) {
	
	}
	
	@Override
	public void onAdvance(double dt) {
	
	}
}
