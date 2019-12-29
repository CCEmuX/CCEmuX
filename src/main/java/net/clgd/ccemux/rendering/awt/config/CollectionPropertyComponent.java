package net.clgd.ccemux.rendering.awt.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class CollectionPropertyComponent<T, TEntry extends CollectionPropertyEntry> extends JComponent {
	private static final long serialVersionUID = 2221453079243528113L;

	private final List<TEntry> entries;
	private final Consumer<T> valueChanged;

	protected final JPanel components;

	public CollectionPropertyComponent(Consumer<T> valueChanged) {
		this.entries = new ArrayList<>();
		this.valueChanged = valueChanged;

		setLayout(new GridBagLayout());

		// We create a child grid view as otherwise the add button
		// gets all messed up when moving elements around.
		{
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1;
			constraints.weighty = 1;
			constraints.fill = GridBagConstraints.BOTH;

			components = new JPanel(new GridBagLayout());
			SwingHelpers.hideBackground(components);
			add(components, constraints);
		}

		{
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.insets = SwingHelpers.SMALL_PADDING;
			constraints.fill = GridBagConstraints.HORIZONTAL;

			JButton addButton = new JButton("Add");
			addButton.setToolTipText("Add a new entry");
			addButton.addActionListener(e -> {
				addEntry(createBlank());
				valueChanged();
				components.revalidate();
				components.repaint();
			});
			add(addButton, constraints);
		}
	}

	protected abstract TEntry createBlank();

	protected abstract T toValue(List<TEntry> rows);

	protected void addEntry(TEntry entry) {
		entry.setRow(entries.size());
		entries.add(entry);
	}

	protected void removeEntry(TEntry entry) {
		int index = entries.indexOf(entry);
		if (index < 0) return;

		// Remove this entry
		entries.remove(index);
		valueChanged();

		// And shift all other elements up
		for (int i = index; i < entries.size(); i++) entries.get(i).setRow(i);
	}

	protected void valueChanged() {
		valueChanged.accept(toValue(entries));
	}
}
