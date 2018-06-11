package net.clgd.ccemux.api.rendering;

import javax.annotation.Nonnull;

import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;

/**
 * A factory used to create a renderer for a given computer
 *
 * @param <T> The type of renderer created by this factory
 */
@FunctionalInterface
public interface RendererFactory<T extends Renderer> {
	/**
	 * Creates a renderer for the given computer and config
	 */
	T create(@Nonnull EmulatedComputer computer, @Nonnull EmuConfig cfg);

	/**
	 * Creates a config editor window for the given config. Returns true if
	 * successful. If unsuccesful or unsupported, {@code false} should be
	 * returned.
	 *
	 * The default implementation returns false and has no side effects.
	 *
	 * @param config The config to let the user edit
	 * @return Whether an editor window was opened
	 */
	default boolean createConfigEditor(@Nonnull EmuConfig config) {
		return false;
	}
}
