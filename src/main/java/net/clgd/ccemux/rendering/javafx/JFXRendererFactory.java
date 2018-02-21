package net.clgd.ccemux.rendering.javafx;

import static javafx.beans.property.DoubleProperty.doubleProperty;
import static net.clgd.ccemux.rendering.javafx.ConfigBindings.wrap;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.val;
import net.clgd.ccemux.emulation.EmuConfig;
import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.rendering.RendererFactory;
import net.clgd.ccemux.rendering.javafx.config.ConfigView;

public class JFXRendererFactory implements RendererFactory<JFXRenderer> {
	private static AtomicBoolean jfxStarted = new AtomicBoolean(false);

	public static class JFXStarter extends Application {
		public static final CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void start(Stage stage) throws Exception {
			latch.countDown();
		}
	}

	private static void startJFX() throws InterruptedException {
		val t = new Thread(() -> Application.launch(JFXStarter.class));
		t.setDaemon(true);
		t.start();

		JFXStarter.latch.await();
	}

	private static synchronized void checkJFX() throws InterruptedException {
		if (!jfxStarted.getAndSet(true)) {
			Platform.setImplicitExit(false);
			startJFX();
		}
	}

	@Override
	public JFXRenderer create(EmulatedComputer computer, EmuConfig cfg) {
		try {
			checkJFX();

			val termScale = doubleProperty(wrap(cfg.termScale));
			val task = new FutureTask<>(() -> new JFXRenderer(new Stage(), computer, termScale));
			Platform.runLater(task);

			return task.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Unable to create JavaFX renderer", e);
		}
	}

	@Override
	public boolean createConfigEditor(EmuConfig config) {
		try {
			checkJFX();

			if (!Platform.isFxApplicationThread()) {
				val task = new FutureTask<>(() -> createConfigEditor(config));
				Platform.runLater(task);
				return task.get();
			} else {
				val stage = new Stage(StageStyle.UTILITY);
				stage.setResizable(true);
				stage.setAlwaysOnTop(true);
				stage.setWidth(800);
				stage.setHeight(600);
				stage.centerOnScreen();
				stage.setScene(new Scene(new ConfigView(config.getRoot()), 800, 600));
				stage.show();
				return true;
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Unable to create JavaFX config editor", e);
		}
	}
}
