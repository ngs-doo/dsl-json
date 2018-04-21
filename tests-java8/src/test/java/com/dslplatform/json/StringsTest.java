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
}
