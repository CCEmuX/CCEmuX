package net.clgd.ccemux.api.emulation;

import java.nio.ByteBuffer;

class LuaHelpers {
	static String decode(ByteBuffer buffer) {
		int position = buffer.position();
		int count = buffer.remaining();

		char[] result = new char[count];
		for (int i = 0; i < count; i++) result[i] = (char) (buffer.get(position + i) & 0xFF);
		return String.valueOf(result);
	}
}
