package net.clgd.ccemux.peripherals;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public abstract class PluginPeripheral implements IPeripheral {
	public abstract String getType();

	public abstract String[] getMethodNames();

	public abstract Object[] callMethod(IComputerAccess iComputerAccess, ILuaContext iLuaContext, int i, Object[] objects) throws LuaException, InterruptedException;

	public abstract void attach(IComputerAccess iComputerAccess);

	public abstract void detach(IComputerAccess iComputerAccess);

	public boolean equals(IPeripheral iPeripheral) {
		return iPeripheral != null && iPeripheral.getClass() == this.getClass();
	}
}
