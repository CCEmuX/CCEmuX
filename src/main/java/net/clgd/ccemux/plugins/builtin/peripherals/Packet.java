package net.clgd.ccemux.plugins.builtin.peripherals;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

public class Packet {
	private final int channel;
	private final int replyChannel;
	private final Object payload;
	private final WirelessModemPeripheral sender;

	public Packet(int channel, int replyChannel, @Nullable Object payload, @Nonnull WirelessModemPeripheral sender) {
		Preconditions.checkNotNull(sender, "sender cannot be null");
		this.channel = channel;
		this.replyChannel = replyChannel;
		this.payload = payload;
		this.sender = sender;
	}

	public int getChannel() {
		return this.channel;
	}

	public int getReplyChannel() {
		return this.replyChannel;
	}

	@Nullable
	public Object getPayload() {
		return this.payload;
	}

	@Nonnull
	public WirelessModemPeripheral getSender() {
		return this.sender;
	}
}
