package net.clgd.ccemux.peripherals;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.clgd.ccemux.emulation.EmulatedComputer;

import java.util.HashMap;
import java.util.Map;

public interface PeripheralFactory {
	public static final Map<String, IPeripheral> implementations = new HashMap<>();
}
