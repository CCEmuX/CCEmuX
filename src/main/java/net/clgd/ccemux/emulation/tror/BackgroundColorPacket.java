package net.clgd.ccemux.emulation.tror;

import net.clgd.ccemux.Utils;

public class BackgroundColorPacket extends TRoRPacket<Character> {
	public BackgroundColorPacket(char color) {
		if (!Utils.BASE_16.contains(color + ""))
			throw new IllegalArgumentException("Invalid color " + color);
		
		data = color;
	}
	
	@Override
	public String getPacketCode() {
		return "TK";
	}
}
