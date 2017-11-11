package net.clgd.ccemux.rendering.awt.config;

import java.awt.GridBagConstraints;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.accessibility.Accessible;
import javax.swing.JButton;
import javax.swing.JComponent;

import lombok.val;
import net.clgd.ccemux.rendering.awt.config.TypedComponentProvider.Factory;

public class ListPropertyComponent<T> extends CollectionPropertyComponent<List<T>, ListPropertyComponent.Entry> implements Accessible {
	private static final long serialVersionUID = 7353851041165416599L;

	private final Factory factory;
	private final Type type;
	private final Object defaultValue;

	public ListPropertyComponent(
			List<T> value, Consumer<List<T>> valueChanged,
			TypedComponentProvider factory, Type type
	) {
		super(valueChanged);
		this.type = type;
		this.factory = factory.getFactory(type).orElse(null);
		this.defaultValue = factory.getDefault(type).orElse(null);

		for (val mapEntry : value) {
			addEntry(new Entry(mapEntry));
		}
	}

	@SuppressWarnings("unchecked")
	protected void addEntry(Entry entry) {
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
	protected void removeEntry(Entry entry) {
		super.removeEntry(entry);

		if (entry.valueComponent != null) components.remove(entry.valueComponent);
		if (entry.removeComponent != null) components.remove(entry.removeComponent);
	}

	@Override
	protected Entry createBlank() {
		return new Entry(defaultValue);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<T> toValue(List<Entry> entries) {
		List<T> result = new ArrayList<>(entries.size());
		for (val entry : entries) result.add((T) entry.value);
		return result;
	}

	public static class Entry implements CollectionPropertyEntry {
		Object value;
		final GridBagConstraints valueConstraint, removeConstraint;
		JComponent valueComponent, removeComponent;

		Entry(Object value) {
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
