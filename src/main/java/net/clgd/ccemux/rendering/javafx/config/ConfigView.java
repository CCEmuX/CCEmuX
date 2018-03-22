package net.clgd.ccemux.rendering.javafx.config;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.val;
import net.clgd.ccemux.config.ConfigProperty;
import net.clgd.ccemux.config.Group;

public class ConfigView extends SplitPane {
	private final TreeView<Group> sidebar;
	private final BorderPane mainView;

	private static final PropertyControlFactory<Object> fallbackFactory = p -> {
		val lbl = new Label("No control factory for type " + p.getType());
		lbl.setStyle("-fx-font-style: italic");
		lbl.setTooltip(new Tooltip(
				"This config option uses an unrecognized data type. No editor control could be created for it."));
		return lbl;
	};

	private static TreeItem<Group> createTree(Group root) {
		val tree = new TreeItem<>(root);

		if (root instanceof Group) {
			((Group) root).children().stream().filter(Group.class::isInstance).map(g -> createTree((Group) g))
					.forEach(tree.getChildren()::add);
		}

		tree.setExpanded(true);

		return tree;
	}

	public ConfigView(Group configRoot) {
		sidebar = new TreeView<>(createTree(configRoot));
		mainView = new BorderPane();

		sidebar.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		sidebar.getSelectionModel().selectedItemProperty()
				.addListener((s, o, n) -> updateMainPane(n == null ? null : n.getValue()));
		sidebar.getSelectionModel().select(sidebar.getRoot());

		getItems().addAll(sidebar, mainView);
		setDividerPositions(0.25);
	}

	private static Node createHeader(Group g) {
		val box = new VBox();

		val title = new Label(g.getName());
		title.setStyle("-fx-font-weight: bold");
		box.getChildren().add(title);

		g.getDescription().ifPresent(d -> {
			val description = new Label(d);
			description.setWrapText(true);
			box.getChildren().add(description);
		});

		return box;
	}

	private static Node createLabelledControl(ConfigProperty<?> p) {
		val label = new Label(p.getName());
		p.getDescription().map(Tooltip::new).ifPresent(label::setTooltip);

		val ctrlFactory = PropertyControlFactory.get(p.getType()).orElse(fallbackFactory);

		val box = new VBox(label, ctrlFactory.createControlFor((ConfigProperty<Object>) p));
		box.setPadding(new Insets(5, 5, 5, 5));
		return box;
	}

	private static Node createCenter(Group g) {
		val center = new VBox();

		g.children().stream().filter(e -> e instanceof ConfigProperty<?>).map(e -> (ConfigProperty<?>) e)
				.map(ConfigView::createLabelledControl).forEach(center.getChildren()::add);

		return new ScrollPane(center);
	}

	private void updateMainPane(Group g) {
		mainView.setTop(g == null ? null : createHeader(g));
		mainView.setCenter(g == null ? null : createCenter(g));
	}
}
