package net.clgd.ccemux.emulation;

import java.nio.file.Path;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.clgd.ccemux.config.Config;
import net.clgd.ccemux.plugins.Plugin;

public abstract class EmuConfig extends Config {
	public EmuConfig(Gson gson, Map<String, JsonElement> data) {
		super(gson, data);
	}
	
	public abstract Path getDataDir();
	
	public Property<Double> termScale = property("termScale", Double.class, 2.0);
	public Property<Integer> termHeight = property("termHeight", Integer.class, 19);
	public Property<Integer> termWidth = property("termWidth", Integer.class, 52);
	public Property<String> renderer = property("renderer", String.class, "AWT");
	public Property<Long> maxComputerCapacity = property("maxComputerCapacity", Long.class, 2L * 1024 * 1024);

	public Property<Boolean> pluginEnabled(Plugin plugin) {
		return property("plugin." + plugin.getClass().getName() + ".enabled", Boolean.class, true);
	}
}
