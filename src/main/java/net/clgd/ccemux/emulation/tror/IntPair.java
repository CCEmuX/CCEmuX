package net.clgd.ccemux.emulation.tror;

import java.util.Objects;

public class IntPair {
	public final int x;
	public final int y;
	
	public IntPair(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		
		IntPair i = (IntPair) o;
		return i.x == x && i.y == y;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
	
	@Override
	public String toString() {
		return x + "," + y;
	}
}
