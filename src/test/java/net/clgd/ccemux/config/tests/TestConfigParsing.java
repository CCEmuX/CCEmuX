package net.clgd.ccemux.config.tests;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.Test;

import net.clgd.ccemux.config.ConfigBindingException;
import net.clgd.ccemux.config.ConfigOption;
import net.clgd.ccemux.config.ParsedConfig;
import net.clgd.ccemux.config.parsers.BooleanParser;
import net.clgd.ccemux.config.parsers.FloatParser;
import net.clgd.ccemux.config.parsers.IntegerParser;
import net.clgd.ccemux.config.parsers.ParseException;
import net.clgd.ccemux.config.parsers.PathParser;
import net.clgd.ccemux.config.parsers.StringParser;
import net.clgd.ccemux.config.parsers.URLParser;

public class TestConfigParsing {
	@Test
	public void testStringParsing() {
		assertEquals("abc", new StringParser().parse("abc"));
	}

	@Test
	public void testIntParsing() throws ParseException {
		assertEquals(1, (int) new IntegerParser().parse("1"));
		assertEquals(-5, (int) new IntegerParser().parse("-5"));
	}

	@Test(expected = ParseException.class)
	public void testIntException() throws ParseException {
		new IntegerParser().parse("3.50");
	}

	@Test
	public void testFloatParsing() throws ParseException {
		assertEquals(1.5f, (float) new FloatParser().parse("1.5"), 1e-7f);
	}

	@Test(expected = ParseException.class)
	public void testFloatException() throws ParseException {
		new FloatParser().parse("4g");
	}

	@Test
	public void testBoolParsing() throws ParseException {
		assertEquals(true, new BooleanParser().parse("true"));
		assertEquals(false, new BooleanParser().parse("false"));
	}

	@Test(expected = ParseException.class)
	public void testBoolException() throws ParseException {
		new BooleanParser().parse("yes");
	}

	@Test
	public void testPathParsing() throws ParseException {
		assertEquals(Paths.get("a/../b"), new PathParser().parse("a/../b"));
	}

	@Test
	public void testURLParsing() throws ParseException, MalformedURLException {
		assertEquals(new URL("http://github.com/Lignumm/CCEmuX"),
				new URLParser().parse("http://github.com/Lignumm/CCEmuX"));
	}

	@Test(expected = ParseException.class)
	public void testURLException() throws ParseException {
		new URLParser().parse("github.com/Lignumm/CCEmuX");
	}
	
	public class TestParsedConfig implements ParsedConfig {
		@ConfigOption(key="str1", parser=StringParser.class, defaultValue="")
		public String str1;
		
		@ConfigOption(key="int1", parser=IntegerParser.class, defaultValue="")
		public int int1;
		
		@ConfigOption(key="int2", parser=IntegerParser.class, defaultValue="")
		public int int2;
		
		@ConfigOption(key="bool1", parser=BooleanParser.class, defaultValue="")
		public boolean bool1;
		
		@ConfigOption(key="path1", parser=PathParser.class, defaultValue="")
		public Path path1;
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testConfigBinding() throws ParseException, ConfigBindingException {
		TestParsedConfig cfg = new TestParsedConfig();
		
		cfg.bindConfigOptions(new HashMap<String,String>() {{
			put("str1", "hello world");
			
			put("int1", "1");
			put("int2", "-5");
			
			put("bool1", "true");
			
			put("path1", "a/b");
		}});
		
		assertEquals(cfg.str1, "hello world");
		
		assertEquals(cfg.int1, 1);
		assertEquals(cfg.int2, -5);
		
		assertEquals(cfg.bool1, true);
		
		assertEquals(cfg.path1, Paths.get("a/b"));
	}
}
