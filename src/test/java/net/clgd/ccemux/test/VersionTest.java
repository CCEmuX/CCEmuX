package net.clgd.ccemux.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import net.clgd.ccemux.emulation.CCEmuX;

public class VersionTest {
	@Test
	public void test() {
		assertNotNull(CCEmuX.getVersion());
	}
}
