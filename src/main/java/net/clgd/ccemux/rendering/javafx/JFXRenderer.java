package net.clgd.ccemux.rendering.javafx;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Value;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.Renderer;

@Value
public class JFXRenderer implements Renderer {
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
		stage.initStyle(StageStyle.UTILITY);
		stage.initModality(Modality.NONE);

		stage.setOnCloseRequest(e -> listeners.forEach(l -> l.onClosed()));
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

	@Override
	public void dispose() {
		Platform.runLater(stage::close);
	}

	private final Set<Listener> listeners = ConcurrentHashMap.newKeySet();

	@Override
	public void addListener(Listener l) {
		listeners.add(l);
	}

	@Override
	public void removeListener(Listener l) {
		listeners.remove(l);
	}
}
