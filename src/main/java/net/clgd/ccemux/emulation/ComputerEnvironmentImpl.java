package net.clgd.ccemux.emulation;

import java.util.Optional;
import java.util.function.Supplier;

import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.ComputerEnvironment;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.metrics.Metric;
import dan200.computercraft.core.metrics.MetricsObserver;

class ComputerEnvironmentImpl implements ComputerEnvironment, MetricsObserver {
	private final CCEmuX emu;
	private final int id;
	private final Supplier<IWritableMount> mount;

	ComputerEnvironmentImpl(CCEmuX emu, int id, Supplier<IWritableMount> mount) {
		this.emu = emu;
		this.id = id;
		this.mount = mount;
	}

	@Override
	public int getDay() {
		return (int) (((emu.getTicksSinceStart() + 6000) / 24000) + 1);
	}

	@Override
	public IWritableMount createRootMount() {
		return Optional.ofNullable(mount)
			.map(Supplier::get)
			.orElseGet(() -> new FileMount(
				emu.getConfig().getComputerDir().resolve(Integer.toString(id)).toFile(),
				emu.getConfig().maxComputerCapacity.get()
			));
	}

	@Override
	public double getTimeOfDay() {
		return ((emu.getTicksSinceStart() + 6000) % 24000) / 1000d;
	}

	@Override
	public MetricsObserver getMetrics() {
		return this;
	}

	@Override
	public void observe(Metric.Counter counter) {
	}

	@Override
	public void observe(Metric.Event event, long value) {
	}
}
