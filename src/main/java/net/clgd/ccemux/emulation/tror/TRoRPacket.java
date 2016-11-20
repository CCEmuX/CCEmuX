package net.clgd.ccemux.emulation.tror;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Objects;

public abstract class TRoRPacket<T> {
	protected T data;
	
	public T data() {
		return data;
	}
	
	public abstract String getPacketCode();
	
	public String toString(String metadata) {
		return getPacketCode() + ':' + metadata + ';' + (firstNonNull(data, "")).toString() + '\n';
	}
	
	@Override
	public String toString() {
		return toString("");
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (!(o instanceof TRoRPacket<?>)) return false;
		
		TRoRPacket<?> p = (TRoRPacket<?>)o;
		if (!getPacketCode().equals(p.getPacketCode())) return false;
		return Objects.equals(data, p.data);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getPacketCode(), data);
	}
}
