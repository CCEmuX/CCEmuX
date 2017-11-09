package net.clgd.ccemux.plugins.builtin;

import com.google.auto.service.AutoService;
import lombok.Getter;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.Plugin;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.RendererFactory;
import net.clgd.ccemux.rendering.gdx.GDXManager;

import java.util.Optional;

@AutoService(Plugin.class)
public class GDXPlugin extends Plugin implements RendererFactory {
	@Getter private GDXManager manager;
	
	@Override
	public String getName() {
		return "GDX Renderer";
	}

	@Override
	public String getDescription() {
		return "A GPU-based renderer using OpenGL and LibGDX.";
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getAuthor() {
		return Optional.of("Lemmmy");
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void setup(EmuConfig cfg) {
		RendererFactory.implementations.put("GDX", this);
	}
	
	@Override
	public Renderer create(EmulatedComputer computer, EmuConfig cfg) {
		if (manager == null) {
			manager = new GDXManager(this, cfg);
		}
		
		return manager.createWindow(computer, cfg);
	}
}
