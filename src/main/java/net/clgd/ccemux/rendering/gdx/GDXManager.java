package net.clgd.ccemux.rendering.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import lombok.Getter;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.builtin.GDXPlugin;
import net.clgd.ccemux.rendering.Renderer;

import java.util.ArrayList;
import java.util.List;

public class GDXManager {
	@Getter private final GDXPlugin plugin;
	
	@Getter private boolean madeFirstWindow = false;
	@Getter private volatile GDXAdapter mainAdapter;
	@Getter private volatile List<GDXAdapter> windows = new ArrayList<>();
	
	public GDXManager(GDXPlugin plugin, EmuConfig config) {
		this.plugin = plugin;
	}
	
	public Renderer createWindow(EmulatedComputer computer, EmuConfig cfg) {
		GDXAdapter adapter = new GDXAdapter(plugin, computer, cfg);
		adapter.startInThread();
		return adapter;
	}
	
	void initialise(GDXAdapter adapter) {
		if (!madeFirstWindow) {
			madeFirstWindow = true;
			mainAdapter = adapter;
			initialiseMainApp(adapter);
		} else {
			initialiseWindow(adapter);
		}
	}
	
	private void setupConfig(GDXAdapter adapter, Lwjgl3WindowConfiguration config) {
		config.setResizable(false); // TODO
		config.setWindowedMode(adapter.getScreenWidth(), adapter.getScreenHeight());
		config.setWindowIcon("img/icon.png");
		config.setWindowListener(new Lwjgl3WindowAdapter() {
			@Override
			public void focusLost() {
				adapter.setFocused(false);
			}
			
			@Override
			public void focusGained() {
				adapter.setFocused(true);
				Gdx.input.setInputProcessor(adapter.getInputMultiplexer());
			}
		});
	}
	
	private void initialiseMainApp(GDXAdapter adapter) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.useVsync(true); // TODO: setting for this
		setupConfig(adapter, config);
		
		new Lwjgl3Application(adapter, config);
	}
	
	private void initialiseWindow(GDXAdapter adapter) {
		Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
		
		Lwjgl3WindowConfiguration config = new Lwjgl3WindowConfiguration();
		setupConfig(adapter, config);
		
		adapter.setWindow(app.newWindow(adapter, config));
	}
}
