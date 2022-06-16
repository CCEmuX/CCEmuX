package net.clgd.ccemux.test;

import net.clgd.ccemux.emulation.CCEmuX;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VersionTest {
	@Test
	public void test() {
		assertNotNull(CCEmuX.getVersion());
	}
}
