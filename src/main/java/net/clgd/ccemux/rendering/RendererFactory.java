package net.clgd.ccemux.rendering;

import java.util.HashMap;
import java.util.Map;

import net.clgd.ccemux.emulation.EmulatedComputer;

@FunctionalInterface
public interface RendererFactory<T extends Renderer> {
	public static final Map<String, RendererFactory<?>> implementations = new HashMap<>();
	
	public T create(EmulatedComputer computer, RendererConfig config);
}
