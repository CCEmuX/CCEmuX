package net.clgd.ccemux.config.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.clgd.ccemux.config.ConfigWriter;

public class TestConfigWriting {

	@Test
	public void testConfigEntryString() {
		assertEquals(new ConfigWriter.ConfigEntry("comment", "key", "value").toString(), "# comment\nkey = value");
	}

}
