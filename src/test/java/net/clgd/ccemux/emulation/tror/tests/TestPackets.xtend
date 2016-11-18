package net.clgd.ccemux.emulation.tror.tests

import net.clgd.ccemux.emulation.tror.BackgroundColorPacket
import net.clgd.ccemux.emulation.tror.BlitLinePacket
import net.clgd.ccemux.emulation.tror.CapabilitiesPacket
import net.clgd.ccemux.emulation.tror.ClearLinePacket
import net.clgd.ccemux.emulation.tror.ClearPacket
import net.clgd.ccemux.emulation.tror.ConnectionClosedPacket
import net.clgd.ccemux.emulation.tror.CursorBlinkPacket
import net.clgd.ccemux.emulation.tror.CursorPosPacket
import net.clgd.ccemux.emulation.tror.ResizePacket
import net.clgd.ccemux.emulation.tror.ScrollPacket
import net.clgd.ccemux.emulation.tror.TextColorPacket
import net.clgd.ccemux.emulation.tror.WritePacket
import org.junit.Test

import static org.junit.Assert.assertEquals

class TestPackets {
	@Test
	def testBackgroundColorPacket() {
		assertEquals("TK:;a\n", new BackgroundColorPacket("a").toString)
		assertEquals("TK:1;0\n", new BackgroundColorPacket("0").toString('1'))
	}
	
	@Test
	def testBlitLinePacket() {
		assertEquals("TY:;aaaa,dddd,text\n", new BlitLinePacket("text", "aaaa", "dddd").toString)
	}
	
	@Test
	def testClearLinePacket() {
		assertEquals("TL:;\n", new ClearLinePacket().toString)
	}
	
	@Test
	def testClearPacket() {
		assertEquals("TE:;\n", new ClearPacket().toString)
	}
	
	@Test
	def testCursorBlinkPacket() {
		assertEquals("TB:;false\n", new CursorBlinkPacket(false).toString)
	}
	
	@Test
	def testCursorPosPacket() {
		assertEquals("TC:;3,5\n", new CursorPosPacket(3, 5).toString)
	}
	
	@Test
	def testResizePacket() {
		assertEquals("TR:;51,19\n", new ResizePacket(51, 19).toString)
	}
	
	@Test
	def testScrollPacket() {
		assertEquals("TS:;3\n", new ScrollPacket(3).toString)
	}
	
	@Test
	def testTextColorPacket() {
		assertEquals("TF:;3\n", new TextColorPacket('3').toString)
	}
	
	@Test
	def testWritePacket() {
		assertEquals("TW:;text\n", new WritePacket("text").toString)
	}
	
	@Test
	def testCapabilitiesPacket() {
		assertEquals("SP:;-ext1-ext2-ext3-\n", new CapabilitiesPacket(#{"ext1", "ext2", "ext3"}).toString)
	}
	
	@Test
	def testConnectionClosedPacket() {
		assertEquals("SC:;\n", new ConnectionClosedPacket().toString)
	}
}