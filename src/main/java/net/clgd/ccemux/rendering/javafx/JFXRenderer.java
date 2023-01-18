package net.clgd.ccemux.rendering.javafx;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.rendering.Renderer;
import net.clgd.ccemux.plugins.builtin.JFXPlugin;

public final class JFXRenderer implements Renderer {
	private final Stage stage;
	private final EmulatedComputer computer;
	private final ReadOnlyDoubleProperty termScale;
	private final ComputerPane pane;

	public JFXRenderer(Stage stage, EmulatedComputer computer, ReadOnlyDoubleProperty termScale) {
		this.stage = stage;
		this.computer = computer;
		this.termScale = termScale;

		this.pane = new ComputerPane(computer, JFXTerminalFont.getBestFont(), termScale);

		Scene scene = new Scene(pane);
		stage.setScene(scene);

		stage.setResizable(false);
		stage.sizeToScene();

		stage.initStyle(JFXPlugin.forceUtilityDecoration.get() ? StageStyle.UTILITY : StageStyle.DECORATED);
		stage.initModality(Modality.NONE);
		stage.setTitle(generateTitle());

		stage.getIcons().add(new Image("/img/icon.png"));

		stage.setOnCloseRequest(e -> listeners.forEach(Listener::onClosed));
	}

	@Override
	public boolean isVisible() {
		return stage.isShowing();
	}

	@Override
	public void setVisible(boolean visible) {
		Platform.runLater(() -> {
			if (visible) {
				stage.show();
				stage.getScene().getRoot().requestFocus();
			} else {
				stage.close();
			}
		});
	}

	private String generateTitle() {
		if (!Strings.isNullOrEmpty(computer.getLabel())) {
			return String.format("CCEmuX - \"%s\" ID %d", computer.getLabel(), computer.getID());
		} else {
			return String.format("CCEmuX - Computer ID %d", computer.getID());
		}
	}

	@Override
	public void onAdvance(double dt) {
		if (!stage.getTitle().equals(generateTitle())) {
			Platform.runLater(() -> stage.setTitle(generateTitle()));
		}
	}

	@Override
	public void dispose() {
		Platform.runLater(stage::close);
	}

	private final Set<Listener> listeners = ConcurrentHashMap.newKeySet();

	@Override
	public void addListener(@Nonnull Listener l) {
		listeners.add(l);
	}

	@Override
	public void removeListener(@Nonnull Listener l) {
		listeners.remove(l);
	}
}
