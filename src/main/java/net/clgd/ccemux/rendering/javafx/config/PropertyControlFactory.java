package net.clgd.ccemux.rendering.javafx.config;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import lombok.val;
import lombok.experimental.UtilityClass;
import net.clgd.ccemux.config.ConfigProperty;
import net.clgd.ccemux.rendering.javafx.ConfigBindings;

@FunctionalInterface
public interface PropertyControlFactory<T> {
	/**
	 * Registers a factory for a given type
	 * 
	 * @param t
	 *            The type
	 * @param f
	 *            The factory for the given type
	 * @deprecated {@link #register(Class, PropertyControlFactory)} should be used
	 *             instead where possible
	 */
	@Deprecated
	public static <T> void register(Type t, PropertyControlFactory<T> f) {
		PropertyControlFactories.register(t, f);
	}

	/**
	 * Registers a factory for a given class
	 * 
	 * @param c
	 *            The class
	 * @param f
	 *            The factory for the given class
	 */
	public static <T> void register(Class<T> c, PropertyControlFactory<T> f) {
		PropertyControlFactories.register(c, f);
	}

	public static <T> Optional<PropertyControlFactory<T>> get(Type t) {
		return PropertyControlFactories.get(t);
	}

	public static <T> Optional<PropertyControlFactory<T>> get(Class<T> c) {
		return PropertyControlFactories.get(c);
	}

	/**
	 * Create a control for the given property. Should ONLY create the control - a
	 * label with the name will be created automatically. The value of the config
	 * property should be updated when the value of the control is updated.
	 * 
	 * @param property
	 * @return
	 */
	public Control createControlFor(ConfigProperty<T> property);
}

@UtilityClass
class PropertyControlFactories {
	private static final Map<Type, PropertyControlFactory<?>> factories = new HashMap<>();

	public static <T> void register(Type t, PropertyControlFactory<T> f) {
		factories.put(t, f);
	}

	public static <T> void register(Class<T> c, PropertyControlFactory<T> f) {
		factories.put(c, f);
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<PropertyControlFactory<T>> get(Type t) {
		return Optional.ofNullable((PropertyControlFactory<T>) factories.get(t));
	}

	// register defaults
	static {
		PropertyControlFactory<Boolean> boolFactory = p -> {
			val cb = new CheckBox();
			cb.selectedProperty().bindBidirectional(ConfigBindings.wrap(p));
			return cb;
		};

		PropertyControlFactory<Double> doubleFactory = p -> {
			val spinner = new Spinner<Double>();
			spinner.setValueFactory(new SpinnerValueFactory<Double>() {
				@Override
				public void decrement(int steps) {
					setValue(getValue() - steps * 0.1);
				}

				@Override
				public void increment(int steps) {
					setValue(getValue() + steps * 0.1);
				}
			});
			spinner.getValueFactory().valueProperty().bindBidirectional(ConfigBindings.wrap(p));
			return spinner;
		};

		PropertyControlFactory<Integer> intFactory = p -> {
			val spinner = new Spinner<Integer>();
			spinner.setValueFactory(new SpinnerValueFactory<Integer>() {
				@Override
				public void decrement(int steps) {
					setValue(getValue() - steps);
				}

				@Override
				public void increment(int steps) {
					setValue(getValue() + steps);
				}

			});
			spinner.getValueFactory().valueProperty().bindBidirectional(ConfigBindings.wrap(p));
			return spinner;
		};

		PropertyControlFactory<Long> longFactory = p -> {
			val spinner = new Spinner<Long>();
			spinner.setValueFactory(new SpinnerValueFactory<Long>() {
				@Override
				public void decrement(int steps) {
					setValue(getValue() - steps);
				}

				@Override
				public void increment(int steps) {
					setValue(getValue() + steps);
				}
			});
			spinner.getValueFactory().valueProperty().bindBidirectional(ConfigBindings.wrap(p));
			return spinner;
		};

		PropertyControlFactory<String> stringFactory = p -> {
			val field = new TextField();
			field.textProperty().bindBidirectional(ConfigBindings.wrap(p));
			return field;
		};

		register(boolean.class, boolFactory);
		register(Boolean.class, boolFactory);

		register(double.class, doubleFactory);
		register(Double.class, doubleFactory);

		register(int.class, intFactory);
		register(Integer.class, intFactory);

		register(long.class, longFactory);
		register(Long.class, longFactory);

		register(String.class, stringFactory);
	}
}
