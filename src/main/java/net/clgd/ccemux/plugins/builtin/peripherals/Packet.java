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

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o != null && this.getClass() == o.getClass()) {
			Packet packet = (Packet) o;
			if (this.channel != packet.channel) {
				return false;
			} else if (this.replyChannel != packet.replyChannel) {
				return false;
			} else {
				if (this.payload != null) {
					if (!this.payload.equals(packet.payload)) {
						return false;
					}
				} else if (packet.payload != null) {
					return false;
				}

				return this.sender.equals(packet.sender);
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		int result = this.channel;
		result = 31 * result + this.replyChannel;
		result = 31 * result + (this.payload != null ? this.payload.hashCode() : 0);
		result = 31 * result + this.sender.hashCode();
		return result;
	}
}
