package net.clgd.ccemux.rendering.javafx.config;

import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.val;
import net.clgd.ccemux.config.ConfigEntry;
import net.clgd.ccemux.config.Group;

public class ConfigView extends SplitPane {
	private final TreeView<Group> sidebar;
	private final BorderPane mainView;

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
		sidebar.getSelectionModel().selectedItemProperty().addListener((s, o, n) -> updateMainPane(n.getValue()));
		sidebar.getSelectionModel().select(sidebar.getRoot());

		getItems().addAll(sidebar, mainView);
		setDividerPositions(0.25);
	}

	private Node createHeader(Group g) {
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

	private void updateMainPane(Group g) {
		mainView.setTop(createHeader(g));

		val controls = new VBox();

		mainView.setCenter(controls);
	}
}
