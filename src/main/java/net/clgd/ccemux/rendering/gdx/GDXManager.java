package net.clgd.ccemux.rendering.gdx;

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
	@Getter private volatile BaseGDXAdapter mainAdapter;
	@Getter private volatile List<BaseGDXAdapter> windows = new ArrayList<>();
	
	public GDXManager(GDXPlugin plugin) {
		this.plugin = plugin;
	}
	
	public Renderer createWindow(EmulatedComputer computer, EmuConfig config) {
		GDXAdapter adapter = new GDXAdapter(plugin, computer, config);
		adapter.startInThread();
		return adapter;
	}
	
	void initialiseAdapter(BaseGDXAdapter adapter) {
		if (!madeFirstWindow) {
			madeFirstWindow = true;
			mainAdapter = adapter;
			adapter.initialise(true);
		} else {
			adapter.initialise(false);
		}
	}
	
	public void createConfigEditor(EmuConfig config) {
		ConfigAdapter adapter = new ConfigAdapter(plugin, config);
		adapter.startInThread();
	}
}
