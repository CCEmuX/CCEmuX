package net.clgd.ccemux.plugins.builtin.peripherals;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.PluginManager;

@AutoService(Plugin.class)
public class DefaultPeripheralsPlugin extends Plugin {
	private ConfigProperty<Long> diskCapacity;

	@Nonnull
	@Override
	public String getName() {
		return "Default Peripherals";
	}

	@Nonnull
	@Override
	public String getDescription() {
		return "Provides various peripherals which are built-in to ComputerCraft";
	}

	@Nonnull
	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Collection<String> getAuthors() {
		return Collections.singleton("CLGD");
	}

	@Nonnull
	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	public void configSetup(@Nonnull Group group) {
		// ComputerCraft.floppySpaceLimit
		diskCapacity = group.property("disk_capacity", Long.class, 125000L)
			.setName("Floppy space limit")
			.setDescription("The disk space limit for floppy disks, in bytes");
	}

	@Override
	public void setup(@Nonnull PluginManager manager) {
		manager.addPeripheral("wireless_modem", (computer, cfg) -> new WirelessModemPeripheral());
		manager.addPeripheral("disk_drive", (computer, cfg) -> new DiskDrivePeripheral(cfg.getDataDir(), diskCapacity));
	}
}
