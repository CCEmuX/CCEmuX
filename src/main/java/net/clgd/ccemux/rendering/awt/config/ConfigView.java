package net.clgd.ccemux.rendering.awt.config;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.*;

import lombok.val;
import net.clgd.ccemux.config.Config;
import net.clgd.ccemux.config.Group;
import net.clgd.ccemux.config.Property;

public class ConfigView extends JFrame {
	private static final long serialVersionUID = 5150153551199976130L;

	private final Deque<Group> history = new ArrayDeque<>();
	private final JPanel scrollBody;
	private final JButton backButton;
	private final JLabel currentGroup;

	public ConfigView(Config config) {
		setLayout(new GridBagLayout());
		setMinimumSize(new Dimension(400, 200));
		setSize(new Dimension(600, 300));
		setTitle("Config");
		setBackground(Color.WHITE);

		{
			backButton = new JButton("Back");
			backButton.setEnabled(false);
			backButton.addActionListener(e -> popGroup());

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.insets = SwingHelpers.PADDING;
			constraints.fill = GridBagConstraints.BOTH;

			add(backButton, constraints);
		}

		{
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.weightx = 1;
			constraints.insets = SwingHelpers.PADDING;
			constraints.fill = GridBagConstraints.BOTH;

			currentGroup = new JLabel("");
			add(currentGroup, constraints);
		}

		{
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.gridwidth = 2;
			constraints.weighty = 1;
			constraints.insets = SwingHelpers.PADDING;
			constraints.fill = GridBagConstraints.BOTH;

			JScrollPane scroll = new JScrollPane();
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			SwingHelpers.hideBackground(scroll);
			SwingHelpers.hideBackground(scroll.getViewport());
			add(scroll, constraints);


			scrollBody = new JPanel(new GridBagLayout());
			SwingHelpers.hideBackground(scrollBody);
			scroll.getViewport().add(scrollBody);
		}

		pushGroup(config.getRoot());

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	private void pushGroup(Group group) {
		history.push(group);
		loadGroup(group);
	}

	private void popGroup() {
		if (history.size() > 1) {
			history.pop();
			loadGroup(history.peek());
		}
	}

	private void loadGroup(Group configGroup) {
		backButton.setEnabled(history.size() > 1);
		currentGroup.setText(configGroup.getName());
		scrollBody.removeAll();

		int row = 0;
		for (val entry : configGroup.children()) {
			if (entry instanceof Property) {
				GridBagConstraints labelConstraints = new GridBagConstraints();
				labelConstraints.gridx = 0;
				labelConstraints.gridy = row;
				labelConstraints.insets = SwingHelpers.PADDING;
				labelConstraints.anchor = GridBagConstraints.EAST;

				JLabel label = new JLabel(entry.getName(), JLabel.RIGHT);
				SwingHelpers.setTooltip(label, entry.getDescription());
				scrollBody.add(label, labelConstraints);

				Property property = (Property) entry;
				val component = TypedComponentProvider.instance().fromProperty(property);
				if (component.isPresent()) {
					GridBagConstraints componentConstraints = new GridBagConstraints();
					componentConstraints.gridx = 1;
					componentConstraints.gridy = row;
					componentConstraints.insets = SwingHelpers.PADDING;
					componentConstraints.weightx = 1;
					componentConstraints.anchor = GridBagConstraints.WEST;
					componentConstraints.fill = GridBagConstraints.HORIZONTAL;
					scrollBody.add(component.get(), componentConstraints);
				}
			} else if (entry instanceof Group) {
				Group group = (Group) entry;

				GridBagConstraints groupConstraints = new GridBagConstraints();
				groupConstraints.gridx = 0;
				groupConstraints.gridy = row;
				groupConstraints.gridwidth = 2;
				groupConstraints.insets = SwingHelpers.PADDING;
				groupConstraints.weightx = 1;
				groupConstraints.anchor = GridBagConstraints.WEST;
				groupConstraints.fill = GridBagConstraints.HORIZONTAL;

				JButton button = new JButton(entry.getName());
				SwingHelpers.setTooltip(button, entry.getDescription());
				button.addActionListener(x -> pushGroup(group));
				scrollBody.add(button, groupConstraints);
			}

			row++;
		}

		{
			JPanel spacer = new JPanel();
			SwingHelpers.hideBackground(spacer);
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = row;
			constraints.weighty = 1;
			constraints.fill = GridBagConstraints.VERTICAL;
			scrollBody.add(spacer, constraints);
		}

		scrollBody.revalidate();
		scrollBody.repaint();
	}

}
