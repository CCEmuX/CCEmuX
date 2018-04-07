package net.clgd.ccemux.plugins.builtin;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.swing.JFrame;

import com.google.auto.service.AutoService;

import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.PluginManager;
import net.clgd.ccemux.api.rendering.Renderer;
import net.clgd.ccemux.api.rendering.RendererFactory;
import net.clgd.ccemux.rendering.awt.AWTRenderer;
import net.clgd.ccemux.rendering.awt.config.ConfigView;

@AutoService(Plugin.class)
public class AWTPlugin extends Plugin {
	private AWTConfig config;

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
	public Collection<String> getAuthors() {
		return Collections.singleton("CLGD");
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void configSetup(Group group) {
		config = new AWTConfig(group);
	}

	@Override
	public void setup(PluginManager manager) {
		manager.addRenderer("AWT", new RendererFactory<Renderer>() {
			private WeakReference<JFrame> lastFrame = null;

			@Override
			public Renderer create(EmulatedComputer computer, EmuConfig cfg) {
				return new AWTRenderer(computer, cfg, config);
			}

			@Override
			public synchronized boolean createConfigEditor(EmuConfig config) {
				if (lastFrame != null) {
					JFrame frame = lastFrame.get();
					if (frame != null && frame.isVisible()) {
						frame.toFront();
						return true;
					}
				}

				JFrame newFrame = new ConfigView(config);
				newFrame.setVisible(true);
				lastFrame = new WeakReference<>(newFrame);

				return true;
			}
		});
	}

	public static class AWTConfig {
		public final ConfigProperty<Boolean> nativePaste;

		AWTConfig(Group group) {
			nativePaste = group.property("nativePaste", boolean.class, false)
					.setName("Use native paste")
					.setDescription("Listen to native paste events instead of Ctrl+V.");
		}
	}
}
