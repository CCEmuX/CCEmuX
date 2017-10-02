package net.clgd.ccemux.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import net.clgd.ccemux.config.ConfigProperty;
import net.clgd.ccemux.config.ConfigPropertyHandler;

public class ConfigHandlerTest {
	public static class TestHandler implements ConfigPropertyHandler<String> {
		@Override
		public String serialize(String data) {
			return "def";
		}

		@Override
		public String deserialize(String serialized) {
			return "ghi";
		}
	}
	
	
	@EqualsAndHashCode
	@ToString
	public static class TestClass {
		@ConfigProperty
		boolean _boolean = true;
		
		@ConfigProperty
		byte _byte = 0x7f;
		
		@ConfigProperty
		char _char = 'a';
		
		@ConfigProperty
		double _double = Double.POSITIVE_INFINITY;
		
		@ConfigProperty
		float _float = Float.NaN;
		
		@ConfigProperty
		int _int = Integer.MAX_VALUE;
		
		@ConfigProperty
		long _long = Long.MAX_VALUE;
		
		@ConfigProperty
		short _short = Short.MIN_VALUE;
		
		@ConfigProperty
		String _string = "hello world";
		
		@ConfigProperty
		Void _void = null;
		
		@ConfigProperty(handler = TestHandler.class)
		String customHandler = "abc";
	}
	
	@Test
	public void testGetConfig() throws IllegalAccessException {
		val expected = new HashMap<String, String>();
		expected.put("_boolean", "false");
		expected.put("_byte", "-128");
		expected.put("_char", "b");
		expected.put("_double", "1.5");
		expected.put("_float", "0.5");
		expected.put("_int", "5");
		expected.put("_long", "10000000000"); // 1e10
		expected.put("_short", "32000");
		expected.put("_string", "hi world");
		expected.put("customHandler", "def");
		
		val object = new TestClass();
		object._boolean = false;
		object._byte = -128;
		object._char = 'b';
		object._double = 1.5d;
		object._float = 0.5f;
		object._int = 5;
		object._long = 10_000_000_000l; // 1e10
		object._short = 32000;
		object._string = "hi world";
		object._void = null;
		object.customHandler = "ghi";
		
		assertEquals(expected, ConfigPropertyHandler.getConfig(object));
	}

	@Test
	public void testWriteConfig() throws IllegalAccessException {
		val values = new HashMap<String, String>();
		values.put("_boolean", "false");
		values.put("_byte", "-128");
		values.put("_char", "b");
		values.put("_double", "1.5");
		values.put("_float", "0.5");
		values.put("_int", "5");
		values.put("_long", "10000000000"); // 1e10
		values.put("_short", "32000");
		values.put("_string", "hi world");
		values.put("customHandler", "abc");
		
		val expected = new TestClass();
		expected._boolean = false;
		expected._byte = -128;
		expected._char = 'b';
		expected._double = 1.5d;
		expected._float = 0.5f;
		expected._int = 5;
		expected._long = 10_000_000_000l; // 1e10
		expected._short = 32000;
		expected._string = "hi world";
		expected._void = null;
		expected.customHandler = "ghi";
		
		assertEquals(expected, ConfigPropertyHandler.apply(new TestClass(), values));
	}
	
	@Test
	public void testSpecialCases() throws IllegalAccessException {
		@EqualsAndHashCode
		@ToString
		class TestClass2 {
			@ConfigProperty
			float nan;
			
			@ConfigProperty
			float infinity;
			
			@ConfigProperty
			float negInfinity;
			
			@ConfigProperty
			float uninitialized;
		}
		
		val expected = new TestClass2();
		
		expected.nan = Float.NaN;
		expected.infinity = Float.POSITIVE_INFINITY;
		expected.negInfinity = Float.NEGATIVE_INFINITY;
		
		val got = ConfigPropertyHandler.apply(new TestClass2(), ConfigPropertyHandler.getConfig(expected));
		
		assertEquals(expected, got);
	}
}
