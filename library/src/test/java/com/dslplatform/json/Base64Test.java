package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class Base64Test {

	@Test
	public void shortIllegalBase64() throws IOException {
		try {
			byte[] base64 = "\"a\"".getBytes("UTF-8");
			DslJson<Object> dsl = new DslJson<Object>();
			dsl.deserialize(byte[].class, base64, base64.length);
			Assert.fail("Expecting end of JSON error");
		} catch (ParsingException e) {
			Assert.assertEquals("Invalid base64 detected at position: 1, following: `\"`, before: `a\"`", e.getMessage());
		}
	}

	@Test
	public void whitespace() throws IOException {
		try {
			byte[] base64 = "\" \"".getBytes("UTF-8");
			DslJson<Object> dsl = new DslJson<Object>();
			dsl.deserialize(byte[].class, base64, base64.length);
			Assert.fail("Expecting end of JSON error");
		} catch (ParsingException e) {
			Assert.assertEquals("Expecting '\"' for base64 end. Found   at position: 1, following: `\"`, before: ` \"`", e.getMessage());
		}
	}

	@Test
	public void shortValidBase64_1() throws IOException {
		byte[] base64 = "\"MQ\"".getBytes("UTF-8");
		DslJson<Object> dsl = new DslJson<Object>();
		byte[] res = dsl.deserialize(byte[].class, base64, base64.length);
		Assert.assertArrayEquals(new byte[]{'1'}, res);
	}

	@Test
	public void shortValidBase64_2() throws IOException {
		byte[] base64 = "\"aa\"".getBytes("UTF-8");
		DslJson<Object> dsl = new DslJson<Object>();
		byte[] res = dsl.deserialize(byte[].class, base64, base64.length);
		Assert.assertArrayEquals(new byte[]{'i'}, res);
	}

	@Test
	public void emptyBase64() throws IOException {
		byte[] base64 = "\"\"".getBytes("UTF-8");
		DslJson<Object> dsl = new DslJson<Object>();
		byte[] res = dsl.deserialize(byte[].class, base64, base64.length);
		Assert.assertEquals(0, res.length);
	}
}
