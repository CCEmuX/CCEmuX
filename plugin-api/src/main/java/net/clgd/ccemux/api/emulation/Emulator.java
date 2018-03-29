package net.clgd.ccemux.api.emulation;

import java.io.File;
import java.util.function.Consumer;

import dan200.computercraft.core.computer.IComputerEnvironment;
import net.clgd.ccemux.api.rendering.Renderer;
import net.clgd.ccemux.api.rendering.RendererFactory;

/**
 * Used to manage running computers
 */
public interface Emulator {
	/**
	 * Gets the version string for this emulator
	 */
	public String getEmulatorVersion();

	/**
	 * Gets the renderer factory in use
	 */
	public <T extends Renderer> RendererFactory<T> getRendererFactory();

	/**
	 * Gets the config used by this config
	 */
	public EmuConfig getConfig();

	/**
	 * Gets the CC jar file
	 */
	public File getCCJar();

	/**
	 * Creates and adds a new computer, returning the created instance
	 * 
	 * @param builderMutator
	 *            A mutator that is passed the computer builder to make any
	 *            necessary changes before the computer is built
	 * @return The computer instance
	 */
	public EmulatedComputer createComputer(Consumer<EmulatedComputer.Builder> builderMutator);

	/**
	 * Creates and adds a new computer, returning the created instance
	 * 
	 * @return The computer instance
	 */
	public default EmulatedComputer createComputer() {
		return createComputer(b -> {});
	}

	/**
	 * Removes the given computer
	 * 
	 * @param computer
	 *            The computer to remove
	 * @return Whether the computer was removed
	 */
	public boolean removeComputer(EmulatedComputer computer);

	/**
	 * Whether the emulator is running
	 */
	public boolean isRunning();

	/**
	 * Stops the emulator if it's currently running
	 */
	public void stop();
}
