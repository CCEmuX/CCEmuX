package net.clgd.ccemux.api.emulation;

import java.io.File;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.clgd.ccemux.api.peripheral.PeripheralFactory;
import net.clgd.ccemux.api.rendering.Renderer;
import net.clgd.ccemux.api.rendering.RendererFactory;

/**
 * Used to manage running computers
 */
public interface Emulator {
	/**
	 * Gets the version string for this emulator
	 */
	@Nonnull
	String getEmulatorVersion();

	/**
	 * Gets the renderer factory in use
	 */
	@Nonnull
	RendererFactory<?> getRendererFactory();

	/**
	 * Get the peripheral factory with the given name
	 *
	 * @param name The peripheral factory to find
	 * @return The peripheral factory or {@code null} if it cannot be found
	 */
	@Nullable
	PeripheralFactory<?> getPeripheralFactory(@Nonnull String name);

	/**
	 * Gets the config used by this config
	 */
	@Nonnull
	EmuConfig getConfig();

	/**
	 * Gets the CC jar file
	 */
	@Nonnull
	File getCCJar();

	/**
	 * Creates and adds a new computer, returning the created instance
	 *
	 * @param builderMutator A mutator that is passed the computer builder to make any
	 *                       necessary changes before the computer is built
	 * @return The computer instance
	 */
	@Nonnull
	EmulatedComputer createComputer(@Nonnull Consumer<EmulatedComputer.Builder> builderMutator);

	/**
	 * Creates and adds a new computer, returning the created instance
	 *
	 * @return The computer instance
	 */
	@Nonnull
	default EmulatedComputer createComputer() {
		return createComputer(b -> {
		});
	}

	/**
	 * Removes the given computer
	 *
	 * @param computer The computer to remove
	 * @return Whether the computer was removed
	 */
	boolean removeComputer(@Nonnull EmulatedComputer computer);

	/**
	 * Whether the emulator is running
	 */
	boolean isRunning();

	/**
	 * Stops the emulator if it's currently running
	 */
	void stop();
}
