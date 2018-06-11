package net.clgd.ccemux.rendering.javafx;

import static javafx.beans.property.DoubleProperty.doubleProperty;
import static net.clgd.ccemux.rendering.javafx.ConfigBindings.wrap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.val;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.rendering.RendererFactory;

public class JFXRendererFactory implements RendererFactory<JFXRenderer> {
	private AtomicBoolean jfxStarted = new AtomicBoolean(false);

	public static class JFXStarter extends Application {
		public static final CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void start(Stage stage) {
			latch.countDown();
		}
	}

	public static void startJFX() throws InterruptedException {
		val t = new Thread(() -> Application.launch(JFXStarter.class));
		t.setDaemon(true);
		t.start();

		JFXStarter.latch.await();
	}

	@Override
	public JFXRenderer create(@Nonnull EmulatedComputer computer, @Nonnull EmuConfig cfg) {
		try {
			if (!jfxStarted.getAndSet(true)) {
				Platform.setImplicitExit(false);
				startJFX();
			}

			val termScale = doubleProperty(wrap(cfg.termScale));
			val task = new FutureTask<>(() -> new JFXRenderer(new Stage(), computer, termScale));
			Platform.runLater(task);

			return task.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Unable to create JavaFX renderer", e);
		}
	}
}
