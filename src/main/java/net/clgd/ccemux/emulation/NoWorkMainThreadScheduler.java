package net.clgd.ccemux.emulation;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import dan200.computercraft.core.computer.mainthread.MainThreadScheduler;

/**
 * A {@link MainThreadScheduler.Executor} instance which refuses to run anything on the main thread. We have no
 * peripherals, so this shouldn't be needed!
 */
final class NoWorkMainThreadScheduler implements MainThreadScheduler.Executor {
	public static final NoWorkMainThreadScheduler INSTANCE = new NoWorkMainThreadScheduler();

	private NoWorkMainThreadScheduler() {
	}

	@Override
	public boolean enqueue(Runnable task) {
		throw new IllegalStateException("Cannot run work on the main thread");
	}

	@Override
	public boolean canWork() {
		return false;
	}

	@Override
	public boolean shouldWork() {
		return false;
	}

	@Override
	public void trackWork(long time, @Nonnull TimeUnit unit) {

	}
}

