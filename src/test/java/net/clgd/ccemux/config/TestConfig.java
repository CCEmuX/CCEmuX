package net.clgd.ccemux.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.Test;

import net.clgd.ccemux.config.parsers.BooleanParser;
import net.clgd.ccemux.config.parsers.FloatParser;
import net.clgd.ccemux.config.parsers.IntegerParser;
import net.clgd.ccemux.config.parsers.ParseException;
import net.clgd.ccemux.config.parsers.PathParser;
import net.clgd.ccemux.config.parsers.StringParser;
import net.clgd.ccemux.config.parsers.URLParser;

public class TestConfig {
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
	
	public class FakeConfig implements Config {
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
		
		@ConfigOption(key="private1", parser=StringParser.class, defaultValue="")
		private String private1;
	}
	
	@SuppressWarnings("serial")
	public static HashMap<String, String> values = new HashMap<String,String>() {{
		put("str1", "hello world");
		
		put("int1", "1");
		put("int2", "-5");
		
		put("bool1", "true");
		
		put("path1", "a" + File.separator + "b");
	}};
	
	@Test
	public void testConfigBinding() throws ParseException, ConfigBindingException {
		FakeConfig cfg = new FakeConfig(); 
		
		cfg.bindConfigOptions(values);
		
		assertEquals("hello world", cfg.str1);
		
		assertEquals(1, cfg.int1);
		assertEquals(-5, cfg.int2);
		
		assertEquals(true, cfg.bool1);
		
		assertEquals(Paths.get("a" + File.separator + "b"), cfg.path1);
	}
	
	@Test
	public void testConfigOutput() throws ConfigBindingException, ParseException {
		FakeConfig cfg = new FakeConfig();
		
		cfg.bindConfigOptions(values);
		
		assertEquals(values, cfg.generateMap(false));
	}
}
