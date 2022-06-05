package net.clgd.ccemux.rendering.awt.config;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Optional;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.clgd.ccemux.api.config.ConfigEntry;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.init.UserConfig;

public class ConfigView extends JFrame {
	private static final long serialVersionUID = 5150153551199976130L;

	private final JPanel scrollBody;
	private final JButton backButton;
	private final JLabel currentGroup;
	private final JTree tree;

	public ConfigView(EmuConfig config) {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(400, 200));
		setSize(new Dimension(600, 300));
		setTitle("Config");
		setBackground(Color.WHITE);

		JSplitPane splitPane = new JSplitPane();
		SwingHelpers.hideBackground(splitPane);
		splitPane.setResizeWeight(0);
		add(splitPane, BorderLayout.CENTER);

		JPanel main = new JPanel(new GridBagLayout());
		SwingHelpers.hideBackground(splitPane);
		splitPane.setRightComponent(main);

		{
			backButton = new JButton("Back");
			backButton.setEnabled(false);
			backButton.addActionListener(e -> popGroup());

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.insets = SwingHelpers.PADDING;
			constraints.fill = GridBagConstraints.BOTH;

			main.add(backButton, constraints);
		}

		{
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.weightx = 1;
			constraints.insets = SwingHelpers.PADDING;
			constraints.fill = GridBagConstraints.BOTH;

			currentGroup = new JLabel("");
			main.add(currentGroup, constraints);
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
			main.add(scroll, constraints);


			scrollBody = new JPanel(new GridBagLayout());
			SwingHelpers.hideBackground(scrollBody);
			scroll.getViewport().add(scrollBody);
		}

		{
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(100, 100));
			scroll.setBorder(null);
			splitPane.setLeftComponent(scroll);

			tree = new JTree(new GroupTreeModel(config.getRoot()));
			tree.setBorder(new EmptyBorder(SwingHelpers.PADDING));
			tree.setCellRenderer(new GroupCellRenderer());
			ToolTipManager.sharedInstance().registerComponent(tree);
			for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
			tree.addTreeSelectionListener(t -> loadGroup((Group) tree.getSelectionPath().getLastPathComponent()));
			scroll.getViewport().add(tree);
		}

		pushGroup(config.getRoot());

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (config instanceof UserConfig) {
					try {
						config.save();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(ConfigView.this,
							"Cannot save config file: " + e.getMessage(),
							"Error saving config", JOptionPane.ERROR_MESSAGE);
					}
				}

				setVisible(false);
				dispose();
			}
		});
	}

	private void pushGroup(Group group) {
		TreePath path = tree.getSelectionPath();
		if (path == null) {
			tree.setSelectionPath(new TreePath(group));
		} else {
			tree.setSelectionPath(path.pathByAddingChild(group));
		}
	}

	private void popGroup() {
		TreePath path = tree.getSelectionPath();
		if (path != null && path.getParentPath() != null) {
			tree.setSelectionPath(path.getParentPath());
		}
	}

	private void loadGroup(Group configGroup) {
		backButton.setEnabled(tree.getSelectionPath().getParentPath() != null);
		currentGroup.setText(configGroup.getName());
		scrollBody.removeAll();

		int row = 0;
		for (ConfigEntry entry : configGroup.children()) {
			if (entry instanceof ConfigProperty) {
				GridBagConstraints labelConstraints = new GridBagConstraints();
				labelConstraints.gridx = 0;
				labelConstraints.gridy = row;
				labelConstraints.insets = SwingHelpers.PADDING;
				labelConstraints.anchor = GridBagConstraints.EAST;

				JLabel label = new JLabel(entry.getName(), JLabel.RIGHT);
				SwingHelpers.setTooltip(label, entry.getDescription());
				scrollBody.add(label, labelConstraints);

				ConfigProperty<?> property = (ConfigProperty<?>) entry;
				Optional<JComponent> component = TypedComponentProvider.instance().fromProperty(property);
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

	private static class GroupTreeModel implements TreeModel {
		private final EventListenerList listenerList = new EventListenerList();
		private final Group root;

		private GroupTreeModel(Group root) {this.root = root;}

		@Override
		public Object getRoot() {
			return root;
		}

		@Override
		public Object getChild(Object parent, int index) {
			Group group = (Group) parent;
			for (ConfigEntry entry : group.children()) {
				if (entry instanceof Group) {
					if (index == 0) {
						return entry;
					} else {
						index--;
					}
				}
			}
			throw new IndexOutOfBoundsException("No child: " + index);
		}

		@Override
		public int getChildCount(Object parent) {
			int count = 0;
			Group group = (Group) parent;
			for (ConfigEntry entry : group.children()) {
				if (entry instanceof Group) count++;
			}
			return count;
		}

		@Override
		public boolean isLeaf(Object node) {
			Group group = (Group) node;
			for (ConfigEntry entry : group.children()) {
				if (entry instanceof Group) return false;
			}
			return true;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			int index = 0;
			Group group = (Group) parent;
			for (ConfigEntry entry : group.children()) {
				if (entry == child) return index;
				if (entry instanceof Group) index++;
			}
			return -1;
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			listenerList.add(TreeModelListener.class, l);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			listenerList.remove(TreeModelListener.class, l);
		}
	}

	protected static class GroupCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = -1372370897360112241L;

		@Override
		public Component getTreeCellRendererComponent(
			JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus
		) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			setIcon(null);
			SwingHelpers.hideBackground(this);
			if (value instanceof ConfigEntry) {
				ConfigEntry entry = (ConfigEntry) value;
				setText(entry.getName());
				SwingHelpers.setTooltip(this, entry.getDescription());
			}
			return this;
		}
	}

}
