package net.clgd.ccemux.plugins.builtin;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;
import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.PluginManager;
import net.clgd.ccemux.rendering.tror.TRoRRenderer;

@AutoService(Plugin.class)
public class TRoRPlugin extends Plugin {
	@Nonnull
	@Override
	public String getName() {
		return "TRoR Renderer";
	}

	@Nonnull
	@Override
	public String getDescription() {
		return "A CPU-based renderer which reads from stdin and writes to stdout using the TRoR protocol.";
	}

	@Nonnull
	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Collection<String> getAuthors() {
		return Collections.singleton("CLGD");
	}

	@Nonnull
	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void setup(@Nonnull PluginManager manager) {
		manager.addRenderer("TRoR", (comp, cfg) -> new TRoRRenderer(comp));
	}
}
