package net.clgd.ccemux.rendering;

import java.util.HashMap;
import java.util.Map;

import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.init.UserConfig;

@FunctionalInterface
public interface RendererFactory<T extends Renderer> {
	public static final Map<String, RendererFactory<?>> implementations = new HashMap<>();
	
	public T create(EmulatedComputer computer, EmuConfig cfg);
}
