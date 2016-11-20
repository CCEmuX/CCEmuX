package net.clgd.ccemux.emulation.tror;

import net.clgd.ccemux.Utils;

public class TextColorPacket extends TRoRPacket<Character> {
	public TextColorPacket(char color) {
		if (!Utils.BASE_16.contains(color + ""))
			throw new IllegalArgumentException("Invalid color " + color);
		
		data = color;
	}
	
	@Override
	public String getPacketCode() {
		return "TF";
	}
}
