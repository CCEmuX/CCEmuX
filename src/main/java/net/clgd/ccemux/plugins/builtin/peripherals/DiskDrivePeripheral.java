package net.clgd.ccemux.plugins.builtin.peripherals;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.filesystem.WritableFileMount;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.peripheral.Peripheral;

/**
 * Emulates ComputerCraft's disk drive
 */
public class DiskDrivePeripheral implements Peripheral {
	private static final Map<Integer, MountInfo> mounts = new HashMap<>();

	private final Path rootPath;
	private final ConfigProperty<Long> capacity;

	private IComputerAccess computer;

	private MountInfo mountInfo;
	private String mountPath;
	private ConfigProperty<Integer> mountId;

	public DiskDrivePeripheral(Path rootPath, ConfigProperty<Long> capacity) {
		this.rootPath = rootPath;
		this.capacity = capacity;
	}

	@Override
	public void configSetup(@Nonnull Group group) {
		mountId = group.property("id", Integer.class, -1)
			.setName("Disk ID")
			.setDescription("The ID of the currently inserted disk, set to -1 to eject");

		mountId.addListener((oldV, newV) -> {
			if (newV < 0) {
				removeMount();
			} else {
				addMount();
			}
		});
	}

	@Nonnull
	@Override
	public String getType() {
		return "drive";
	}

	@LuaFunction
	public final boolean isDiskPresent() {
		return mountId.get() >= 0;
	}

	@LuaFunction
	public final Object[] getDiskLabel() {
		MountInfo info = mountInfo;
		return info != null ? new Object[]{info.label} : null;
	}

	@LuaFunction
	public final void setDiskLabel(Optional<String> label) {
		MountInfo info = mountInfo;
		if (info != null) info.label = label.orElse(null);
	}

	@LuaFunction
	public final boolean hasData() {
		return mountPath != null;
	}

	@LuaFunction
	public final String getMountPath() {
		return mountPath;
	}

	@LuaFunction
	public final boolean hasAudio() {
		return false;
	}

	@LuaFunction
	public final Object getAudioTitle() {
		return mountId.get() >= 0 ? null : false;
	}

	@LuaFunction
	public final void playAudio() {}

	@LuaFunction
	public final void stopAudio() {}

	@LuaFunction
	public final void ejectDisk() {
		mountId.set(-1);
	}

	@LuaFunction
	public final Object[] getDiskID() {
		int id = mountId.get();
		return id >= 0 ? new Object[id] : null;
	}

	@Override
	public synchronized void attach(@Nonnull IComputerAccess computer) {
		this.computer = computer;
		addMount();
	}

	@Override
	public void detach(@Nonnull IComputerAccess computer) {
		removeMount();
		this.computer = null;
	}

	private synchronized void addMount() {
		int id = mountId.get();
		if (id < 0 || computer == null) return;

		MountInfo mountInfo;
		synchronized (mounts) {
			mountInfo = mounts.get(id);
			if (mountInfo == null) {
				mounts.put(id, mountInfo = new MountInfo(new WritableFileMount(
					rootPath.resolve("computer").resolve("disk").resolve(Integer.toString(mountId.get())).toFile(),
					capacity.get()
				)));
			}
		}

		String mountPath = null;
		for (int n = 1; mountPath == null; ++n) {
			mountPath = computer.mountWritable("disk" + (n == 1 ? "" : Integer.toString(n)), mountInfo.mount);
		}

		this.mountInfo = mountInfo;
		this.mountPath = mountPath;

		computer.queueEvent("disk", new Object[]{computer.getAttachmentName()});
	}

	private synchronized void removeMount() {
		if (mountPath == null) return;

		computer.unmount(mountPath);
		computer.queueEvent("disk_eject", new Object[]{computer.getAttachmentName()});
		mountPath = null;
	}

	/**
	 * Represents information about a particular disk. This is shared across all
	 * instances of a given disk ID, ensuring {@link WritableFileMount}'s usage/capacity tracking
	 * is consistent.
	 */
	private static class MountInfo {
		String label;
		WritableMount mount;

		MountInfo(WritableMount mount) {
			this.mount = mount;
		}
	}
}
