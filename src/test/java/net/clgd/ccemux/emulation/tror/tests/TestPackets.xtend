package net.clgd.ccemux.emulation.tror.tests

import net.clgd.ccemux.emulation.tror.BackgroundColorPacket
import net.clgd.ccemux.emulation.tror.BlitLinePacket
import net.clgd.ccemux.emulation.tror.ClearLinePacket
import net.clgd.ccemux.emulation.tror.ClearPacket
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
		assertEquals("TK:;a", new BackgroundColorPacket("a").toString)
		assertEquals("TK:1;0", new BackgroundColorPacket("0").toString('1'))
	}
	
	@Test
	def testBlitLinePacket() {
		assertEquals("TY:;aaaa,dddd,text", new BlitLinePacket("text", "aaaa", "dddd").toString)
	}
	
	@Test
	def testClearLinePacket() {
		assertEquals("TL:;", new ClearLinePacket().toString)
	}
	
	@Test
	def testClearPacket() {
		assertEquals("TE:;", new ClearPacket().toString)
	}
	
	@Test
	def testCursorBlinkPacket() {
		assertEquals("TB:;false", new CursorBlinkPacket(false).toString)
	}
	
	@Test
	def testCursorPosPacket() {
		assertEquals("TC:;3,5", new CursorPosPacket(3, 5).toString)
	}
	
	@Test
	def testResizePacket() {
		assertEquals("TR:;51,19", new ResizePacket(51, 19).toString)
	}
	
	@Test
	def testScrollPacket() {
		assertEquals("TS:;3", new ScrollPacket(3).toString)
	}
	
	@Test
	def testTextColorPacket() {
		assertEquals("TF:;3", new TextColorPacket('3').toString)
	}
	
	@Test
	def testWritePacket() {
		assertEquals("TW:;text", new WritePacket("text").toString)
	}
}