package net.clgd.ccemux.rendering.javafx.config;

import javafx.scene.control.Control;
import net.clgd.ccemux.config.ConfigProperty;

@FunctionalInterface
public interface PropertyControlFactory<T> {
	/**
	 * Create a control for the given property. Should ONLY create the control -
	 * a label with the name will be created automatically. The value of the
	 * config property should be updated when the value of the control is
	 * updated.
	 * 
	 * @param property
	 * @return
	 */
	public Control createControlFor(ConfigProperty<T> property);
}
