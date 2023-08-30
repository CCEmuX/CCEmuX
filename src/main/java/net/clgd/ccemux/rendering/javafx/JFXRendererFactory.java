package net.clgd.ccemux.rendering.javafx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Stage;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.rendering.RendererFactory;

public class JFXRendererFactory implements RendererFactory<JFXRenderer> {
	private final AtomicBoolean jfxStarted = new AtomicBoolean(false);

	public static class JFXStarter extends Application {
		public static final CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void start(Stage stage) {
			latch.countDown();
		}
	}

	public static void startJFX() throws InterruptedException {
		Thread t = new Thread(() -> Application.launch(JFXStarter.class));
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

			DoubleProperty termScale = new SimpleDoubleProperty(null, "termScale", computer.getTermScale());
			FutureTask<JFXRenderer> task = new FutureTask<>(() -> new JFXRenderer(new Stage(), computer, termScale));
			Platform.runLater(task);

			return task.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Unable to create JavaFX renderer", e);
		}
	}
}
