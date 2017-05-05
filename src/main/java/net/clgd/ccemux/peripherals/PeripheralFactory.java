package net.clgd.ccemux.peripherals;

import net.clgd.ccemux.emulation.EmulatedComputer;

import java.util.HashMap;
import java.util.Map;

public interface PeripheralFactory {
	public static final Map<String, PluginPeripheral> implementations = new HashMap<>();
}
