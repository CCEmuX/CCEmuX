package net.clgd.ccemux.plugins.builtin;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.swing.JFrame;

import com.google.auto.service.AutoService;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.Plugin;
import net.clgd.ccemux.rendering.Renderer;
import net.clgd.ccemux.rendering.RendererFactory;
import net.clgd.ccemux.rendering.awt.AWTRenderer;
import net.clgd.ccemux.rendering.awt.config.ConfigView;

@AutoService(Plugin.class)
public class AWTPlugin extends Plugin {
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
	public void setup(EmuConfig cfg) {
		RendererFactory.implementations.put("AWT", new RendererFactory<Renderer>() {
			private WeakReference<JFrame> lastFrame = null;

			@Override
			public Renderer create(EmulatedComputer computer, EmuConfig cfg) {
				return new AWTRenderer(computer, cfg);
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
}
