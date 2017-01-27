package net.clgd.ccemux.rendering;

import java.util.HashMap;
import java.util.Map;

public class RenderingMethods {
	private static Map<String, Class<? extends Renderer>> implementations = new HashMap<>();

	/**
	 * Adds a rendering method which may be used later
	 * 
	 * @param klass
	 *            The class of the implementation
	 * @param name
	 *            The name of the rendering method
	 * @throws IllegalArgumentException
	 *             Thrown when a rendering method with the given name is already
	 *             present
	 */
	public static void addImplementation(Class<? extends Renderer> klass, String name) {
		if (implementations.containsKey(name))
			throw new IllegalArgumentException("A rendering method with the name \"" + name + "\" is already present");
		implementations.put(name, klass);
	}

	/**
	 * Gets a rendering method by name
	 * 
	 * @param name
	 *            The name of the implementation
	 * @return The rendering method class, or <code>null</code> if not present
	 */
	public static Class<? extends Renderer> getImplementation(String name) {
		return implementations.get(name);
	}

	/**
	 * Gets a map of all registered rendering methods (name to implementation
	 * class)
	 */
	public static Map<String, Class<? extends Renderer>> getAllImplementations() {
		return implementations;
	}

	private RenderingMethods() {
	}
}
