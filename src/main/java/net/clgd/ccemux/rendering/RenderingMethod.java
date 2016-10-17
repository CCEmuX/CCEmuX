package net.clgd.ccemux.rendering;

import java.util.function.BiFunction;
import net.clgd.ccemux.emulation.CCEmuX;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.swing.SwingRenderer;

public enum RenderingMethod {
	Headless((CCEmuX emu, EmulatedComputer comp) -> new Renderer() {
		@Override
		public boolean isVisible() {
			return false;
		}

		@Override
		public void setVisible(boolean visible) {

		}

		@Override
		public void onUpdate(float dt) {

		}

		@Override
		public void onDispose() {

		}
	}),

	Swing((CCEmuX emu, EmulatedComputer comp) -> new SwingRenderer(emu, comp));

	private final BiFunction<CCEmuX, EmulatedComputer, Renderer> creator;

	private RenderingMethod(BiFunction<CCEmuX, EmulatedComputer, Renderer> creator) {
		this.creator = creator;
	}

	private Renderer create(CCEmuX emu, EmulatedComputer computer) {
		return creator.apply(emu, computer);
	}

	public static Renderer create(String type, CCEmuX emu, EmulatedComputer computer) {
		synchronized (computer) {
			for (RenderingMethod r : values()) {
				if (r.name().equals(type)) {
					emu.getLogger().debug("Creating {} renderer", r.name());
					return r.create(emu, computer);
				}
			}

			emu.getLogger().error("Could not create renderer of type {}", type);
			throw new IllegalArgumentException("Invalid renderer type " + type);
		}
	}

	public static RenderingMethod[] getMethods() {
		return values();
	}
}
