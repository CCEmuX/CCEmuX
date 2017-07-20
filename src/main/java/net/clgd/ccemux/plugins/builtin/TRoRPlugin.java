package net.clgd.ccemux.plugins.builtin;

import java.util.Optional;

import net.clgd.ccemux.plugins.Plugin;
import net.clgd.ccemux.rendering.RendererFactory;
import net.clgd.ccemux.rendering.tror.TRoRRenderer;

public class TRoRPlugin extends Plugin {
	@Override
	public String getName() {
		return "TRoR Renderer";
	}

	@Override
	public String getDescription() {
		return "A CPU-based renderer which reads from stdin and writes to stdout using the TRoR protocol.";
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
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void setup() {
		RendererFactory.implementations.put("TRoR", TRoRRenderer::new);
	}
}
