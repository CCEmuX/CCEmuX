package net.clgd.ccemux.plugins.builtin.peripherals;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.google.auto.service.AutoService;
import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.peripheral.Peripheral;
import net.clgd.ccemux.api.peripheral.PeripheralFactory;
import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.PluginManager;

@AutoService(Plugin.class)
public class DefaultPeripheralsPlugin extends Plugin {
	@Override
	public String getName() {
		return "Default Peripherals";
	}

	@Override
	public String getDescription() {
		return "Provides various peripherals which are built-in to ComputerCraft";
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Override
	public Collection<String> getAuthors() {
		return Collections.singleton("CLGD");
	}

	@Override
	public Optional<String> getWebsite() {
		return Optional.empty();
	}

	@Override
	@SuppressWarnings("Convert2Lambda")
	public void setup(PluginManager manager) {
		// We need to use anonymous classes in order to defer the loading of IPeripheral
		manager.addPeripheral("wireless_modem", new PeripheralFactory<Peripheral>() {
			@Override
			public Peripheral create(EmulatedComputer c, EmuConfig e) {
				return new WirelessModemPeripheral();
			}
		});
	}
}
