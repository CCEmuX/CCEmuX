package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.*;
import lombok.Getter;
import lombok.Setter;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;

import javax.annotation.Nullable;

@Getter @Setter
public abstract class BaseGDXAdapter extends ApplicationAdapter {
	protected final GDXPlugin plugin;
	
	protected volatile boolean focused;
	@Nullable protected Lwjgl3Window window; // TODO: can be null if main window
	
	protected volatile Thread thread;
	
	protected volatile InputMultiplexer inputMultiplexer;
	
	public BaseGDXAdapter(GDXPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void create() {
		super.create();
		
		inputMultiplexer = new InputMultiplexer();
	}
	
	void startInThread() {
		thread = new Thread(() -> plugin.getManager().initialiseAdapter(this));
		thread.start();
	}
	
	void filesDropped(String[] files) {}
	
	void initialise(boolean mainWindow) {
		if (mainWindow) {
			Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
			config.useVsync(true); // TODO: setting for this
			setupConfig(config);
			
			new Lwjgl3Application(this, config);
		} else {
			Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
			
			Lwjgl3WindowConfiguration config = new Lwjgl3WindowConfiguration();
			setupConfig(config);
			
			window = app.newWindow(this, config);
		}
	}
	
	void setupConfig(Lwjgl3WindowConfiguration config) {
		config.setWindowIcon("img/icon.png");
		config.setWindowListener(new Lwjgl3WindowAdapter() {
			@Override
			public void focusLost() {
				focused = false;
			}
			
			@Override
			public void focusGained() {
				focused = true;
				Gdx.input.setInputProcessor(inputMultiplexer);
			}
			
			@Override
			public void filesDropped(String[] files) {
				filesDropped(files);
			}
		});
	}
	
	void setTitle(String title) {
		if (window != null) {
			window.setTitle(title);
		} else {
			Gdx.graphics.setTitle(title);
		}
	}
}
