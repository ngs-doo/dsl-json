package com.dslplatform.json;

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
	public void expectedDefaults() throws IOException {
		byte[] input = "{}".getBytes("UTF-8");
		Strings s = dslJson.deserialize(Strings.class, input, input.length);
		Assert.assertEquals("", s.s1);
		Assert.assertEquals(null, s.s2);
	}

	@Test
	public void cantBeNull() throws IOException {
		byte[] input = "{\"s1\":null}".getBytes("UTF-8");
		try {
			dslJson.deserialize(Strings.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 's1' is not allowed to be null at position: 10"));
		}
	}

	@Test
	public void canBeNull() throws IOException {
		byte[] input = "{\"s2\":null}".getBytes("UTF-8");
		Strings s = dslJson.deserialize(Strings.class, input, input.length);
		Assert.assertEquals("", s.s1);
		Assert.assertEquals(null, s.s2);
	}
}
