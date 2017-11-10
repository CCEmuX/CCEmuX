package net.clgd.ccemux.rendering.awt.config;

import java.awt.GridBagConstraints;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.accessibility.Accessible;
import javax.swing.JButton;
import javax.swing.JComponent;

import lombok.val;
import net.clgd.ccemux.rendering.awt.config.TypedComponentProvider.Factory;

public class MapPropertyComponent<K, V> extends CollectionPropertyComponent<Map<K, V>, MapPropertyComponent.Entry> implements Accessible {
	private static final long serialVersionUID = -5168107222176592501L;

	private final Factory keyFactory, valueFactory;
	private final Type keyType, valueType;
	private final Object keyDefault, valueDefault;

	public MapPropertyComponent(
			Map<K, V> value, Consumer<Map<K, V>> valueChanged,
			TypedComponentProvider factory, Type keyType, Type valueType
	) {
		super(valueChanged);
		this.keyType = keyType;
		this.valueType = valueType;
		this.keyFactory = factory.getFactory(keyType).orElse(null);
		this.valueFactory = factory.getFactory(valueType).orElse(null);
		this.keyDefault = factory.getDefault(keyType).orElse(null);
		this.valueDefault = factory.getDefault(valueType).orElse(null);

		for (val mapEntry : value.entrySet()) {
			addEntry(new Entry(mapEntry.getKey(), mapEntry.getValue()));
		}
	}

	@SuppressWarnings("unchecked")
	protected void addEntry(Entry entry) {
		super.addEntry(entry);

		if (keyFactory != null) {
			components.add(entry.keyComponent = keyFactory.create(entry.key, x -> {
				entry.key = x;
				valueChanged();
			}, keyType), entry.keyConstraint);
		}

		if (valueFactory != null) {
			components.add(entry.valueComponent = valueFactory.create(entry.value, x -> {
				entry.value = x;
				valueChanged();
			}, valueType), entry.valueConstraint);
		}

		JButton button = new JButton("X");
		button.setToolTipText("Remove this entry");
		button.addActionListener(e -> {
			removeEntry(entry);
			components.revalidate();
			components.repaint();
		});
		components.add(entry.removeComponent = button, entry.removeConstraint);
	}

	@Override
	protected void removeEntry(Entry entry) {
		super.removeEntry(entry);

		if (entry.keyComponent != null) components.remove(entry.keyComponent);
		if (entry.valueComponent != null) components.remove(entry.valueComponent);
		if (entry.removeComponent != null) components.remove(entry.removeComponent);
	}

	@Override
	protected Entry createBlank() {
		return new Entry(keyDefault, valueDefault);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Map<K, V> toValue(List<Entry> entries) {
		Map<K, V> result = new LinkedHashMap<>(entries.size());
		for (val entry : entries) result.put((K) entry.key, (V) entry.value);
		return result;
	}

	public static class Entry implements CollectionPropertyEntry {
		Object key, value;
		final GridBagConstraints keyConstraint, valueConstraint, removeConstraint;
		JComponent keyComponent, valueComponent, removeComponent;

		Entry(Object key, Object value) {
			this.key = key;
			this.value = value;

			keyConstraint = new GridBagConstraints();
			keyConstraint.gridx = 0;
			keyConstraint.weightx = 1;
			keyConstraint.insets = SwingHelpers.SMALL_PADDING;
			keyConstraint.fill = GridBagConstraints.HORIZONTAL;

			valueConstraint = new GridBagConstraints();
			valueConstraint.gridx = 1;
			valueConstraint.weightx = 1;
			valueConstraint.insets = SwingHelpers.SMALL_PADDING;
			valueConstraint.fill = GridBagConstraints.HORIZONTAL;

			removeConstraint = new GridBagConstraints();
			removeConstraint.gridx = 2;
			removeConstraint.insets = SwingHelpers.SMALL_PADDING;
		}

		@Override
		public void setRow(int index) {
			keyConstraint.gridy = valueConstraint.gridy = removeConstraint.gridy = index;
		}
	}
}
