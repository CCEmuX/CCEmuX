package net.clgd.ccemux.plugins.builtin;

import java.util.*;

import com.google.auto.service.AutoService;

import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.plugins.Plugin;
import net.clgd.ccemux.rendering.RendererFactory;
import net.clgd.ccemux.rendering.javafx.JFXRendererFactory;

@AutoService(Plugin.class)
public class JFXPlugin extends Plugin {
	@Override
	public String getName() {
		return "JavaFX Renderer";
	}

	@Override
	public String getDescription() {
		return "A renderer using JavaFX";
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Override
	public Collection<String> getAuthors() {
		return Collections.singleton("CLGD");
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void setup(EmuConfig cfg) {
		RendererFactory.implementations.put("JFX", new JFXRendererFactory());
	}
}
