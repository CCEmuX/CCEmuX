package net.clgd.ccemux.rendering;

import java.util.function.Function;

import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.awt.AWTRenderer;
import net.clgd.ccemux.rendering.tror.TRoRRenderer;;

public enum RenderingMethod {
	Headless((EmulatedComputer comp) -> new Renderer() {
		@Override
		public boolean isVisible() {
			return false;
		}

		@Override
		public void setVisible(boolean visible) { }

		@Override
		public void resize(int width, int height) { }

		@Override
		public void onAdvance(double dt) { }

		@Override
		public void onDispose() { }

		@Override
		public void onTerminalResized(int width, int height) { }
	}),

	AWT(AWTRenderer::new),
	TRoR_STDIO(TRoRRenderer::new);

	private final Function<EmulatedComputer, Renderer> creator;

	private RenderingMethod(Function<EmulatedComputer, Renderer> creator) {
		this.creator = creator;
	}

	private Renderer create(EmulatedComputer computer) {
		return creator.apply(computer);
	}

	public static Renderer create(String type, EmulatedComputer computer) {
		synchronized (computer) {
			for (RenderingMethod method : values()) {
				if (method.name().equals(type)) {
					computer.emu.logger.debug("Creating {} renderer", method.name());
					Renderer renderer = method.create(computer);
					computer.addListener(renderer);
					return renderer;
				}
			}

			computer.emu.logger.error("Could not create renderer of type {}", type);
			throw new IllegalArgumentException("Invalid renderer type " + type);
		}
	}

	public static RenderingMethod[] getMethods() {
		return values();
	}
}
