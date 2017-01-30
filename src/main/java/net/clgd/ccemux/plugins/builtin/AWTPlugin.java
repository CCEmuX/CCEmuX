package net.clgd.ccemux.plugins.builtin;

import java.net.URL;
import java.util.Optional;

import net.clgd.ccemux.plugins.Plugin;
import net.clgd.ccemux.rendering.RenderingMethods;
import net.clgd.ccemux.rendering.awt.AWTRenderer;

public class AWTPlugin implements Plugin {

	@Override
	public String getName() {
		return "AWT Renderer";
	}

	@Override
	public String getDescription() {
		return "A CPU-based renderer using Java AWT.";
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getAuthor() {
		return Optional.of("CLGD");
	}

	@Override
	public Optional<URL> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void setup() {
		RenderingMethods.addImplementation(AWTRenderer.class, "AWT");
	}

}
