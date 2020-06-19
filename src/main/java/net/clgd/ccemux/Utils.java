package net.clgd.ccemux;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import dan200.computercraft.api.lua.ILuaCallback;
import dan200.computercraft.api.lua.MethodResult;
import net.clgd.ccemux.api.emulation.EmulatedComputer;

public final class Utils {
	private static final int RETRIES = 10;
	private static Random random = new Random();

	private Utils() {
	}

	/**
	 * Create a unique file with a prefix and suffix, appending random digits if the file already exists.
	 *
	 * @param directory The directory to create the file in.
	 * @param name      The prefix of the name.
	 * @param extension The suffix of the file, including the ".".
	 * @return The generated file
	 * @throws IOException If we cannot create a random file
	 */
	public static synchronized File createUniqueFile(File directory, String name, String extension) throws IOException {
		File file = new File(directory, name + extension);
		if (file.createNewFile()) return file;
		return File.createTempFile(name + ".", extension, directory);
	}

	private static class Box {
		Object[] contents;
	}

	private static final String TASK_EVENT = "ccemux_task";

	private static final AtomicInteger taskId = new AtomicInteger();

	/**
	 * Observe a {@link ListenableFuture} and resume the computer when it has completed.
	 *
	 * @param computer The computer to resume.
	 * @param future   The future to observe.
	 * @param success  A function to process the results when the future succeeds.
	 * @param failure  A function to process the exception when the future fails. Currently this cannot throw a
	 *                 exception.
	 * @return The result of {@code success} or {@code failure}
	 */
	public static <T> MethodResult awaitFuture(EmulatedComputer computer, ListenableFuture<T> future, Function<T, MethodResult> success, Function<Throwable, MethodResult> failure) {
		int id = taskId.getAndIncrement();
		Task task = new Task(id);
		future.addListener(() -> {
			try {
				task.result = success.apply(future.get());
			} catch (InterruptedException e) {
				task.result = failure.apply(e);
			} catch (ExecutionException e) {
				task.result = failure.apply(e.getCause());
			}

			computer.queueEvent(TASK_EVENT, new Object[] { id });
		}, MoreExecutors.directExecutor());

		return task.pullEvent;
	}

	private static class Task implements ILuaCallback {
		private final int id;

		final MethodResult pullEvent = MethodResult.pullEvent(TASK_EVENT, this);
		MethodResult result;

		Task(int id) {
			this.id = id;
		}

		@Nonnull
		@Override
		public MethodResult resume(Object[] objects) {
			if (objects.length >= 2 && objects[1] instanceof Number && ((Number) objects[1]).intValue() == id) {
				return result;
			}

			return pullEvent;
		}
	}
}
