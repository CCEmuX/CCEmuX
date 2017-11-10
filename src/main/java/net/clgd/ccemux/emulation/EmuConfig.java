package net.clgd.ccemux.emulation;

import java.nio.file.Path;

import com.google.gson.Gson;
import net.clgd.ccemux.config.Config;
import net.clgd.ccemux.config.JsonAdapter;
import net.clgd.ccemux.config.Property;

public abstract class EmuConfig extends Config {
	protected final JsonAdapter adapter;

	public EmuConfig(Gson gson) {
		adapter = new JsonAdapter(gson);
	}

	public abstract Path getDataDir();

	public Property<Double> termScale = property("termScale", Double.class, 2.0)
			.setName("Terminal scale");
	public Property<Integer> termHeight = property("termHeight", Integer.class, 19)
			.setName("Terminal height");
	public Property<Integer> termWidth = property("termWidth", Integer.class, 52)
			.setName("Terminal width");
	public Property<String> renderer = property("renderer", String.class, "AWT")
			.setName("Renderer");
	public Property<Long> maxComputerCapacity = property("maxComputerCapacity", Long.class, 2L * 1024 * 1024)
			.setName("Max computer capacity");
}
