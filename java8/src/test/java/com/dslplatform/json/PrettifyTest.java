package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PrettifyTest {

	private final PrettifyStream ps = new PrettifyStream();

	@Test
	public void number() throws IOException {
		String output = ps.process(" 123.4 ");
		Assert.assertEquals("123.4", output);
	}

	@Test
	public void string() throws IOException {
		String output = ps.process("\n\"1234\"   ");
		Assert.assertEquals("\"1234\"", output);
	}

	@Test
	public void specialStrings() throws IOException {
		String output = ps.process("[\"1\\2\\\"34\",\"\"]");
		Assert.assertEquals("[\n  \"1\\2\\\"34\",\n  \"\"\n]", output);
	}

	@Test
	public void constant() throws IOException {
		String output = ps.process("false");
		Assert.assertEquals("false", output);
	}

	@Test
	public void objectInArray() throws IOException {
		String output = ps.process("[{\"abc\":123},{\"abc\":234}]");
		Assert.assertEquals("[\n  {\n    \"abc\": 123\n  },\n  {\n    \"abc\": 234\n  }\n]", output);
	}

	@Test
	public void stuffInArray() throws IOException {
		String output = ps.process("[true,false,null,{\"abc\":[]},{\"abc\":234}]");
		Assert.assertEquals("[\n  true,\n  false,\n  null,\n  {\n    \"abc\": []\n  },\n  {\n    \"abc\": 234\n  }\n]", output);
	}

	@Test
	public void stuffInObject() throws IOException {
		String output = ps.process("{\"a\":true,\"b\":false,\"c\":null,\"d\":{\"abc\":[]},\"e\":{\"abc\":234}}");
		Assert.assertEquals("{\n  \"a\": true,\n  \"b\": false,\n  \"c\": null,\n  \"d\": {\n    \"abc\": []\n  },\n  \"e\": {\n    \"abc\": 234\n  }\n}", output);
	}

	@Test
	public void emptyObjectInArray() throws IOException {
		String output = ps.process("[{}]");
		Assert.assertEquals("[\n  {}\n]", output);
	}

	@Test
	public void willNotCloseStream() throws IOException {
		final boolean[] closed = new boolean[2];
		ByteArrayInputStream is = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8)) {
			@Override
			public void close() {
				closed[0] = true;
			}
		};
		ByteArrayOutputStream os = new ByteArrayOutputStream() {
			@Override
			public void close() {
				closed[1] = true;
			}
		};
		ps.process(is, os);
		Assert.assertFalse(closed[0]);
		Assert.assertFalse(closed[1]);
		Assert.assertEquals("[]", os.toString("UTF-8"));
	}
}
