package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StringsTest {

	@CompiledJson
	public static class BuildersAndBuffers {
		public StringBuilder builder;
		public StringBuffer buffer;
	}

	@CompiledJson
	public static class Strings {
		@JsonAttribute(nullable = false)
		public String s1;
		public String s2;
		@JsonAttribute(nullable = false)
		public String[] s3;
		public String[] s4;
	}

	public static class StringsWithFactory {
		@JsonAttribute(nullable = false)
		public String s1;
		public String s2;
		@JsonAttribute(nullable = false)
		public String[] s3;
		public String[] s4;

		@CompiledJson
		public static StringsWithFactory Create(String s1, String s2, String[] s3, String[] s4) {
			StringsWithFactory swf = new StringsWithFactory();
			swf.s1 = s1;
			swf.s2 = s2;
			swf.s3 = s3;
			swf.s4 = s4;
			return swf;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void roundtrip() throws IOException {
		BuildersAndBuffers bb = new BuildersAndBuffers();
		bb.builder = new StringBuilder("abcd");
		bb.buffer = new StringBuffer("def");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(bb, os);
		byte[] input = os.toByteArray();
		BuildersAndBuffers s = dslJson.deserialize(BuildersAndBuffers.class, input, input.length);
		Assert.assertEquals("abcd", s.builder.toString());
		Assert.assertEquals("def", s.buffer.toString());
	}

	@Test
	public void expectedDefaultsWithCtor() throws IOException {
		byte[] input = "{}".getBytes("UTF-8");
		Strings s = dslJson.deserialize(Strings.class, input, input.length);
		Assert.assertEquals("", s.s1);
		Assert.assertEquals(null, s.s2);
		Assert.assertEquals(0, s.s3.length);
		Assert.assertEquals(null, s.s4);
	}

	@Test
	public void expectedDefaultsWithFactory() throws IOException {
		byte[] input = "{}".getBytes("UTF-8");
		StringsWithFactory s = dslJson.deserialize(StringsWithFactory.class, input, input.length);
		Assert.assertEquals("", s.s1);
		Assert.assertEquals(null, s.s2);
		Assert.assertEquals(0, s.s3.length);
		Assert.assertEquals(null, s.s4);
	}

	@Test
	public void simplePropertyCantBeNull() throws IOException {
		byte[] input = "{\"s1\":null}".getBytes("UTF-8");
		try {
			dslJson.deserialize(Strings.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 's1' is not allowed to be null at position: 10"));
		}
	}

	@Test
	public void arrayCantBeNull() throws IOException {
		byte[] input = "{\"s3\":null}".getBytes("UTF-8");
		try {
			dslJson.deserialize(Strings.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 's3' is not allowed to be null at position: 10"));
		}
	}

	@Test
	public void simplePropertyCanBeNull() throws IOException {
		byte[] input = "{\"s2\":null}".getBytes("UTF-8");
		Strings s = dslJson.deserialize(Strings.class, input, input.length);
		Assert.assertEquals("", s.s1);
		Assert.assertEquals(null, s.s2);
		Assert.assertEquals(0, s.s3.length);
		Assert.assertEquals(null, s.s4);
	}

	@Test
	public void arrayCanBeNull() throws IOException {
		byte[] input = "{\"s4\":null}".getBytes("UTF-8");
		Strings s = dslJson.deserialize(Strings.class, input, input.length);
		Assert.assertEquals("", s.s1);
		Assert.assertEquals(null, s.s2);
		Assert.assertEquals(0, s.s3.length);
		Assert.assertEquals(null, s.s4);
	}

	@Test
	public void nullSimplePropertyWillThrow() throws IOException {
		DslJson<Object> dslJsonMinimal = new DslJson<>(Settings.basicSetup().skipDefaultValues(true));
		for (DslJson<Object> json : new DslJson[]{dslJson, dslJsonMinimal}) {
			try {
				dslJson.serialize(new Strings(), new ByteArrayOutputStream());
				Assert.fail("Expecting exception");
			} catch (ConfigurationException ex) {
				Assert.assertTrue(ex.getMessage().contains("Property 's1' is not allowed to be null"));
			}
		}
	}

	@Test
	public void nullArrayPropertyWillThrow() throws IOException {
		Strings s = new Strings();
		s.s1 = "";
		DslJson<Object> dslJsonMinimal = new DslJson<>(Settings.basicSetup().skipDefaultValues(true));
		for (DslJson<Object> json : new DslJson[]{dslJson, dslJsonMinimal}) {
			try {
				dslJson.serialize(s, new ByteArrayOutputStream());
				Assert.fail("Expecting exception");
			} catch (ConfigurationException ex) {
				Assert.assertTrue(ex.getMessage().contains("Property 's3' is not allowed to be null"));
			}
		}
	}
}
