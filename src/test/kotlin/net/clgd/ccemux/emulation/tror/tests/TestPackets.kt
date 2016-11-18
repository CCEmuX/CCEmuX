package net.clgd.ccemux.emulation.tror.tests

import net.clgd.ccemux.emulation.tror.*
import org.junit.Assert.assertEquals
import org.junit.Test

class TestPackets {
	@Test fun testBackgroundColorPacket() {
		assertEquals("TK:;a\n", BackgroundColourPacket('a').toString())
		assertEquals("TK:1;0\n", BackgroundColourPacket('0').toString("1"))
	}

	@Test fun testBlitLinePacket() {
		assertEquals("TY:;aaaa,dddd,text\n", BlitLinePacket("text", "aaaa", "dddd").toString())
	}

	@Test fun testClearLinePacket() {
		assertEquals("TL:;\n", ClearLinePacket().toString())
	}

	@Test fun testClearPacket() {
		assertEquals("TE:;\n", ClearPacket().toString())
	}

	@Test fun testCursorBlinkPacket() {
		assertEquals("TB:;false\n", CursorBlinkPacket(false).toString())
	}

	@Test fun testCursorPosPacket() {
		assertEquals("TC:;3,5\n", CursorPosPacket(3, 5).toString())
	}

	@Test fun testResizePacket() {
		assertEquals("TR:;51,19\n", ResizePacket(51, 19).toString())
	}

	@Test fun testScrollPacket() {
		assertEquals("TS:;3\n", ScrollPacket(3).toString())
	}

	@Test fun testTextColorPacket() {
		assertEquals("TF:;3\n", TextColourPacket('3').toString())
	}

	@Test fun testWritePacket() {
		assertEquals("TW:;text\n", WritePacket("text").toString())
	}

	@Test fun testCapabilitiesPacket() {
		assertEquals("SP:;-ext1-ext2-ext3-\n", CapabilitiesPacket(setOf("ext1", "ext2", "ext3")).toString())
	}

	@Test fun testConnectionClosedPacket() {
		assertEquals("SC:;\n", ConnectionClosedPacket().toString())
	}
}
