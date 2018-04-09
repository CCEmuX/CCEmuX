package net.clgd.ccemux.plugins.builtin.peripherals;

import static dan200.computercraft.core.apis.ArgumentHelper.optString;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.filesystem.FileMount;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.peripheral.Peripheral;

/**
 * Emulates ComputerCraft's disk drive
 *
 * @see dan200.computercraft.shared.peripheral.diskdrive.DiskDrivePeripheral
 * @see dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive
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
	public void configSetup(Group group) {
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

	@Nonnull
	@Override
	public String[] getMethodNames() {
		return new String[] {
			"isDiskPresent",
			"getDiskLabel",
			"setDiskLabel",
			"hasData",
			"getMountPath",
			"hasAudio",
			"getAudioTitle",
			"playAudio",
			"stopAudio",
			"ejectDisk",
			"getDiskID"
		};
	}

	@Override
	public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) throws LuaException {
		switch (method) {
			case 0: // isPresent
				return new Object[] { mountId.get() >= 0 };
			case 1: { // getDiskLabel
				MountInfo info = mountInfo;
				return info != null ? new Object[] { info.label } : null;
			}
			case 2: { // setDiskLabel
				String label = optString(arguments, 0, null);

				MountInfo info = mountInfo;
				if (info != null) info.label = label;
				return null;
			}
			case 3:// hasData
				return new Object[] { mountPath != null };
			case 4:// getMountPath
				return new Object[] { mountPath };
			case 5: // hasAudio
				return new Object[] { false };
			case 6: // getAudioTitle
				return new Object[] { mountId.get() >= 0 ? null : false };
			case 7: // playAudio
				return null;
			case 8: // stopAudio
				return null;
			case 9: { // eject
				mountId.set(-1);
				return null;
			}
			case 10: { // getDiskID
				int id = mountId.get();
				return id >= 0 ? new Object[id] : null;
			}
			default:
				return null;
		}
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
				mounts.put(id, mountInfo = new MountInfo(new FileMount(
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

		computer.queueEvent("disk", new Object[] { computer.getAttachmentName() });
	}

	private synchronized void removeMount() {
		if (mountPath == null) return;

		computer.unmount(mountPath);
		computer.queueEvent("disk_eject", new Object[] { computer.getAttachmentName() });
		mountPath = null;
	}

	/**
	 * Represents information about a particular disk. This is shared across all
	 * instances of a given disk ID, ensuring {@link FileMount}'s usage/capacity tracking
	 * is consistent.
	 */
	private static class MountInfo {
		String label;
		IWritableMount mount;

		MountInfo(IWritableMount mount) {
			this.mount = mount;
		}
	}
}
