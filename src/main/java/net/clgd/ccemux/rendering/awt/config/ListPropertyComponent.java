package net.clgd.ccemux.rendering.awt.config;

import java.awt.GridBagConstraints;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.accessibility.Accessible;
import javax.swing.JButton;
import javax.swing.JComponent;

import net.clgd.ccemux.rendering.awt.config.TypedComponentProvider.Factory;

public class ListPropertyComponent<T> extends CollectionPropertyComponent<List<T>, ListPropertyComponent.Entry<T>> implements Accessible {
	private static final long serialVersionUID = 7353851041165416599L;

	private final Factory<T> factory;
	private final Type type;
	private final T defaultValue;

	@SuppressWarnings("unchecked")
	public ListPropertyComponent(
		List<T> value, Consumer<List<T>> valueChanged,
		TypedComponentProvider factory, Type type
	) {
		super(valueChanged);
		this.type = type;
		this.factory = (Factory<T>)factory.getFactory(type).orElse(null);
		this.defaultValue = (T)factory.getDefault(type).orElse(null);

		for (T mapEntry : value) {
			addEntry(new Entry<>(mapEntry));
		}
	}

	@Override
	protected void addEntry(Entry<T> entry) {
		super.addEntry(entry);

		if (factory != null) {
			components.add(entry.valueComponent = factory.create(entry.value, x -> {
				entry.value = x;
				valueChanged();
			}, type), entry.valueConstraint);
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
	protected void removeEntry(Entry<T> entry) {
		super.removeEntry(entry);

		if (entry.valueComponent != null) components.remove(entry.valueComponent);
		if (entry.removeComponent != null) components.remove(entry.removeComponent);
	}

	@Override
	protected Entry<T> createBlank() {
		return new Entry<>(defaultValue);
	}

	@Override
	protected List<T> toValue(List<Entry<T>> entries) {
		List<T> result = new ArrayList<>(entries.size());
		for (Entry<T> entry : entries) result.add(entry.value);
		return result;
	}

	public static class Entry<T> implements CollectionPropertyEntry {
		T value;
		final GridBagConstraints valueConstraint, removeConstraint;
		JComponent valueComponent, removeComponent;

		Entry(T value) {
			this.value = value;

			valueConstraint = new GridBagConstraints();
			valueConstraint.gridx = 0;
			valueConstraint.weightx = 1;
			valueConstraint.insets = SwingHelpers.SMALL_PADDING;
			valueConstraint.fill = GridBagConstraints.HORIZONTAL;

			removeConstraint = new GridBagConstraints();
			removeConstraint.gridx = 1;
			removeConstraint.insets = SwingHelpers.SMALL_PADDING;
		}

		@Override
		public void setRow(int index) {
			valueConstraint.gridy = removeConstraint.gridy = index;
		}
	}
}
