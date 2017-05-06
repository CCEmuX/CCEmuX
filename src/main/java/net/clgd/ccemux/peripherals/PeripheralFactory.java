package net.clgd.ccemux.peripherals;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.clgd.ccemux.emulation.EmulatedComputer;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface PeripheralFactory<T extends IPeripheral> {
	public static final Map<String, PeripheralFactory<?>> implementations = new HashMap<>();

	public T create();
}
