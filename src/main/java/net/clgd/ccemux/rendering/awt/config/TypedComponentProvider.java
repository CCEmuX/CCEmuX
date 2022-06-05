package net.clgd.ccemux.rendering.awt.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.common.primitives.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonPrimitive;
import net.clgd.ccemux.api.config.ConfigProperty;

@SuppressWarnings("rawtypes")
public class TypedComponentProvider {
	private static final TypedComponentProvider instance = new TypedComponentProvider();

	public interface Factory<T> {
		JComponent create(T value, Consumer<T> valueChanged, Type baseType);
	}

	private final Map<Class<?>, Factory<?>> types = new HashMap<>();
	private final Map<Class<?>, Object> defaultValues = new HashMap<>();

	public <T> void register(Class<T> type, T defaultValue, Factory<T> property) {
		types.put(type, property);
		defaultValues.put(type, defaultValue);

		// Automatically register the wrapped/unwrapped equivalent
		if (Primitives.isWrapperType(type)) {
			types.put(Primitives.unwrap(type), property);
			defaultValues.put(Primitives.unwrap(type), defaultValue);
		} else if (type.isPrimitive()) {
			types.put(Primitives.wrap(type), property);
			defaultValues.put(Primitives.wrap(type), defaultValue);
		}
	}

	private static Type extractBase(Type type) {
		if (type instanceof Class<?>) {
			return type;
		} else if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).getRawType();
		} else {
			return type;
		}
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	public Optional<Factory<?>> getFactory(Type type) {
		return Optional.ofNullable(types.get(extractBase(type)));
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	public Optional<Object> getDefault(Type type) {
		return Optional.ofNullable(defaultValues.get(extractBase(type)));
	}

	@SuppressWarnings("unchecked")
	public Optional<JComponent> fromProperty(ConfigProperty property) {
		return getFactory(property.getType())
			.map((Factory x) -> x.create(property.get(), property::set, property.getType()));
	}

	public static TypedComponentProvider instance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public TypedComponentProvider() {
		register(int.class, 0, (value, callback, ty) -> {
			SpinnerNumberModel model = new SpinnerNumberModel((int) value, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
			JSpinner spinner = new JSpinner(model);
			spinner.addChangeListener(e -> callback.accept(model.getNumber().intValue()));
			return spinner;
		});

		register(long.class, 0L, (value, callback, ty) -> {
			SpinnerNumberModel model = new SpinnerNumberModel((long) value, Long.MIN_VALUE, Long.MAX_VALUE, 1);
			JSpinner spinner = new JSpinner(model);
			spinner.addChangeListener(e -> callback.accept(model.getNumber().longValue()));
			return spinner;
		});

		register(double.class, 0d, (value, callback, ty) -> {
			SpinnerNumberModel model = new SpinnerNumberModel(value, null, null, 1);
			JSpinner spinner = new JSpinner(model);
			spinner.addChangeListener(e -> callback.accept(model.getNumber().doubleValue()));
			return spinner;
		});

		register(boolean.class, false, (value, callback, ty) -> {
			JCheckBox checkbox = new JCheckBox();
			checkbox.getModel().setSelected(value);
			checkbox.setBackground(null);
			checkbox.addItemListener(e -> callback.accept(checkbox.getModel().isSelected()));
			return checkbox;
		});

		register(String.class, "", (value, callback, ty) -> {
			JTextField text = new JTextField();
			text.setText(value);
			text.getDocument().addDocumentListener(new ChangeListener(() -> callback.accept(text.getText())));
			return text;
		});

		register(JsonPrimitive.class, new JsonPrimitive(""), (value, callback, ty) -> {
			JTextField text = new JTextField();
			text.setText(value == null ? "" : value.getAsString());
			text.getDocument().addDocumentListener(new ChangeListener(() ->
				callback.accept(new JsonPrimitive(text.getText()))));
			return text;
		});

		register(Map.class, Collections.emptyMap(), (value, callback, ty) -> {
			TypeToken base = TypeToken.of(ty);
			ParameterizedType ofMap = (ParameterizedType) base.getSupertype(Map.class).getType();
			Type[] arguments = ofMap.getActualTypeArguments();
			return new MapPropertyComponent(value, callback, this, arguments[0], arguments[1]);
		});

		register(List.class, Collections.emptyList(), (value, callback, ty) -> {
			TypeToken base = TypeToken.of(ty);
			ParameterizedType ofList = (ParameterizedType) base.getSupertype(List.class).getType();
			Type[] arguments = ofList.getActualTypeArguments();
			return new ListPropertyComponent(value, callback, this, arguments[0]);
		});

		register(int[].class, new int[0], (value, callback, ty) ->
			new ListPropertyComponent<>(Ints.asList(value), c -> callback.accept(Ints.toArray(c)), this, Integer.class));

		register(long[].class, new long[0], (value, callback, ty) ->
			new ListPropertyComponent<>(Longs.asList(value), c -> callback.accept(Longs.toArray(c)), this, Long.class));

		register(double[].class, new double[0], (value, callback, ty) ->
			new ListPropertyComponent<>(Doubles.asList(value), c -> callback.accept(Doubles.toArray(c)), this, Double.class));

		register(boolean[].class, new boolean[0], (value, callback, ty) ->
			new ListPropertyComponent<>(Booleans.asList(value), c -> callback.accept(Booleans.toArray(c)), this, Booleans.class));

		register(String[].class, new String[0], (value, callback, ty) ->
			new ListPropertyComponent<>(Arrays.asList(value), c -> callback.accept(c.toArray(new String[c.size()])), this, String.class));
	}

	private static class ChangeListener implements DocumentListener {
		private final Runnable action;

		private ChangeListener(Runnable action) {
			this.action = action;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			action.run();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			action.run();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			action.run();
		}
	}
}
