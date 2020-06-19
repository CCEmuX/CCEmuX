package net.clgd.ccemux.plugins.builtin.peripherals;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.peripheral.Peripheral;

/**
 * Emulates ComputerCraft's wireless and ender modem
 *
 * @see dan200.computercraft.shared.peripheral.modem.wireless.WirelessModemPeripheral
 * @see dan200.computercraft.shared.peripheral.modem.ModemPeripheral
 */
public class WirelessModemPeripheral implements Peripheral {
	private static final Set<WirelessModemPeripheral> modems = new HashSet<>();

	private IComputerAccess computer = null;
	private final Set<Integer> channels = new HashSet<>();
	private boolean open = false;

	private ConfigProperty<Integer> range;
	private ConfigProperty<Boolean> interdimensional;
	private ConfigProperty<String> world;
	private ConfigProperty<Integer> posX;
	private ConfigProperty<Integer> posY;
	private ConfigProperty<Integer> posZ;

	@Override
	public void configSetup(@Nonnull Group group) {
		range = group.property("range", Integer.class, 64);
		interdimensional = group.property("interdimensional", Boolean.class, false);

		world = group.property("world", String.class, "main");
		posX = group.property("posX", Integer.class, 0);
		posY = group.property("posY", Integer.class, 0);
		posZ = group.property("posZ", Integer.class, 0);
	}

	private void receiveSameDimension(@Nonnull Packet packet, double distance) {
		if (packet.getSender() != this) {
			synchronized (this) {
				if (computer != null && channels.contains(packet.getChannel())) {
					computer.queueEvent("modem_message", computer.getAttachmentName(), packet.getChannel(), packet.getReplyChannel(), packet.getPayload(), distance);
				}
			}
		}
	}

	private void receiveDifferentDimension(@Nonnull Packet packet) {
		if (packet.getSender() != this) {
			synchronized (this) {
				if (computer != null && channels.contains(packet.getChannel())) {
					computer.queueEvent("modem_message", computer.getAttachmentName(), packet.getChannel(), packet.getReplyChannel(), packet.getPayload());
				}
			}
		}
	}

	@Override
	@Nonnull
	public String getType() {
		return "modem";
	}

	private static void checkChannel(int channel) throws LuaException {
		if (channel < 0 || channel > 65535) throw new LuaException("Expected number in range 0-65535");
	}

	@LuaFunction
	public final void open(int channel) throws LuaException {
		checkChannel(channel);
		synchronized (this) {
			if (!channels.contains(channel)) {
				if (channels.size() >= 128) {
					throw new LuaException("Too many open channels");
				}

				channels.add(channel);
				open = true;
			}
		}
	}

	@LuaFunction
	public final boolean isOpen(int channel) throws LuaException {
		checkChannel(channel);
		synchronized (this) {
			return channels.contains(channel);
		}
	}

	@LuaFunction
	public final void close(int channel) throws LuaException {
		checkChannel(channel);
		synchronized (this) {
			if (channels.remove(channel) && channels.size() == 0) open = false;
		}
	}

	@LuaFunction
	public final void closeAll() {
		synchronized (this) {
			if (channels.size() > 0) {
				channels.clear();
				open = false;
			}
		}
	}

	@LuaFunction
	public final void transmit(int channel, int replyChannel, Object payload) throws LuaException {
		checkChannel(channel);
		checkChannel(replyChannel);

		synchronized (this) {
			Packet packet = new Packet(channel, replyChannel, payload, this);
			synchronized (modems) {
				for (WirelessModemPeripheral receiver : modems) receiver.tryTransmit(packet);
			}
		}
	}

	@LuaFunction
	public final boolean isWireless() {
		return true;
	}

	private void tryTransmit(Packet packet) {
		WirelessModemPeripheral sender = packet.getSender();
		if (world.get().equals(sender.world.get())) {
			double receiveRange = Math.max(sender.range.get(), range.get());
			double distanceSq
				= Math.pow(posX.get() - sender.posX.get(), 2)
				+ Math.pow(posY.get() - sender.posY.get(), 2)
				+ Math.pow(posZ.get() - sender.posZ.get(), 2);

			if (interdimensional.get() || sender.interdimensional.get() || distanceSq <= receiveRange * receiveRange) {
				receiveSameDimension(packet, Math.sqrt(distanceSq));
			}
		} else if (interdimensional.get() || sender.interdimensional.get()) {
			receiveDifferentDimension(packet);
		}
	}

	@Override
	public synchronized void attach(@Nonnull IComputerAccess computer) {
		this.computer = computer;
		open = !channels.isEmpty();
		synchronized (modems) {
			modems.add(this);
		}
	}

	@Override
	public synchronized void detach(@Nonnull IComputerAccess computer) {
		synchronized (modems) {
			modems.remove(this);
		}
		channels.clear();
		this.computer = null;
		if (open) open = false;
	}
}
