package net.clgd.ccemux.rendering.javafx;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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
	private final TerminalCanvas canvas;

	public JFXRenderer(Stage stage, EmulatedComputer computer, ReadOnlyDoubleProperty termScale) {
		this.stage = stage;
		this.computer = computer;
		this.termScale = termScale;

		this.canvas = new TerminalCanvas(computer.terminal, JFXTerminalFont.getBestFont(), termScale);

		Scene scene = new Scene(new StackPane(canvas));
		stage.setScene(scene);
		stage.setResizable(false);

		stage.initStyle(StageStyle.UTILITY);
		
		stage.setOnCloseRequest(e -> listeners.forEach(l -> l.onClosed()));
	}

	@Override
	public void onAdvance(double dt) {
		if (computer.terminal.getChanged()) {
			computer.terminal.clearChanged();
			canvas.redraw();
		}
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
			} else {
				stage.close();
			}
		});
	}

	@Override
	public void dispose() {
		Platform.runLater(stage::close);
	}

	private Set<Listener> listeners = ConcurrentHashMap.newKeySet();

	@Override
	public void addListener(Listener l) {
		listeners.add(l);
	}

	@Override
	public void removeListener(Listener l) {
		listeners.remove(l);
	}
}
