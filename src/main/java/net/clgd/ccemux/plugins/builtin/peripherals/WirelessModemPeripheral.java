package net.clgd.ccemux.plugins.builtin.peripherals;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.apis.ArgumentHelper;
import net.clgd.ccemux.api.config.ConfigProperty;
import net.clgd.ccemux.api.config.Group;
import net.clgd.ccemux.api.peripheral.Peripheral;

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
	public void configSetup(Group group) {
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
					computer.queueEvent("modem_message", new Object[] { computer.getAttachmentName(), packet.getChannel(), packet.getReplyChannel(), packet.getPayload(), distance });
				}
			}
		}
	}

	private void receiveDifferentDimension(@Nonnull Packet packet) {
		if (packet.getSender() != this) {
			synchronized (this) {
				if (computer != null && channels.contains(packet.getChannel())) {
					computer.queueEvent("modem_message", new Object[] { computer.getAttachmentName(), packet.getChannel(), packet.getReplyChannel(), packet.getPayload() });
				}
			}
		}
	}

	@Nonnull
	public String getType() {
		return "modem";
	}

	@Nonnull
	public String[] getMethodNames() {
		return new String[] {
			"open",
			"isOpen",
			"close",
			"closeAll",
			"transmit",
			"isWireless",
		};
	}

	private static int parseChannel(Object[] arguments, int index) throws LuaException {
		int channel = ArgumentHelper.getInt(arguments, index);
		if (channel >= 0 && channel <= 65535) {
			return channel;
		} else {
			throw new LuaException("Expected number in range 0-65535");
		}
	}

	public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0: { // open
				int channel = parseChannel(arguments, 0);
				synchronized (this) {
					if (!channels.contains(channel)) {
						if (channels.size() >= 128) {
							throw new LuaException("Too many open channels");
						}

						channels.add(channel);
						open = true;
					}
				}

				return null;
			}
			case 1: { // isOpen
				int channel = parseChannel(arguments, 0);
				synchronized (this) {
					boolean open = channels.contains(channel);
					return new Object[] { open };
				}
			}
			case 2: { // close
				int channel = parseChannel(arguments, 0);
				synchronized (this) {
					if (channels.remove(channel) && channels.size() == 0) open = false;

					return null;
				}
			}
			case 3: // closeAll
				synchronized (this) {
					if (channels.size() > 0) {
						channels.clear();
						open = false;
					}

					return null;
				}
			case 4: { // transmit
				int channel = parseChannel(arguments, 0);
				int replyChannel = parseChannel(arguments, 1);
				Object payload = arguments.length >= 3 ? arguments[2] : null;
				synchronized (this) {
					Packet packet = new Packet(channel, replyChannel, payload, this);
					synchronized (modems) {
						for (WirelessModemPeripheral receiver : modems) receiver.tryTransmit(packet);
					}
					return null;
				}
			}
			case 5: // isWireless
				return new Object[] { true };
			default:
				return null;
		}
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

	public synchronized void attach(@Nonnull IComputerAccess computer) {
		this.computer = computer;
		open = !channels.isEmpty();
		synchronized (modems) {
			modems.add(this);
		}
	}

	public synchronized void detach(@Nonnull IComputerAccess computer) {
		synchronized (modems) {
			modems.remove(this);
		}
		channels.clear();
		this.computer = null;
		if (open) open = false;
	}

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}
}
