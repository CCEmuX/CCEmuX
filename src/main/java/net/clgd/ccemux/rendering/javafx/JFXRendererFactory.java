package net.clgd.ccemux.rendering.javafx;

import static javafx.beans.property.DoubleProperty.doubleProperty;
import static net.clgd.ccemux.rendering.javafx.ConfigBindings.wrap;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.val;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.RendererFactory;

public class JFXRendererFactory implements RendererFactory<JFXRenderer> {
	private AtomicBoolean jfxStarted = new AtomicBoolean(false);

	@Override
	public JFXRenderer create(EmulatedComputer computer, EmuConfig cfg) {
		val task = new FutureTask<>(() -> new JFXRenderer(new Stage(), computer, doubleProperty(wrap(cfg.termScale))));

		if (!jfxStarted.getAndSet(true)) {
			Platform.setImplicitExit(false);
			Platform.startup(task);
		} else {
			Platform.runLater(task);
		}

		try {
			return task.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Error creating new JavaFX renderer", e);
		}
	}
}
