package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class ReaderTest {

	@Test
	public void testLastName() throws IOException {
		final byte[] buf = "\"number\":1234".getBytes("UTF-8");
		final JsonReader<Object> jr = new JsonReader<Object>(buf, null);
		jr.getNextToken();
		jr.fillName();
		Assert.assertEquals("number", jr.getLastName());
		jr.getNextToken();
		Assert.assertTrue(jr.wasLastName("number"));
		int num = NumberConverter.deserializeInt(jr);
		Assert.assertEquals(1234, num);
	}

	@Test
	public void testCalcHashNameEndSameAsFillName() throws IOException {
		final byte[] buf = "\"number\":1234".getBytes("UTF-8");
		final JsonReader<Object> jr = new JsonReader<Object>(buf, null);
		jr.getNextToken();
		jr.calcHash();
		Assert.assertTrue(jr.wasLastName("number"));
		Assert.assertEquals("number", jr.getLastName());
	}

	static class Implementation implements Interface {
	}

	interface Interface {
	}

	@Test
	public void testReaderOnInterface() throws IOException {
		DslJson<Object> dslJson = new DslJson<Object>();
		dslJson.registerReader(Implementation.class, new JsonReader.ReadObject<Implementation>() {
			@Override
			public Implementation read(JsonReader reader) throws IOException {
				return null;
			}
		});
		JsonReader.ReadObject<?> reader1 = dslJson.tryFindReader(Interface.class);
		Assert.assertNull(reader1);
		JsonReader.ReadObject<?> reader2 = dslJson.tryFindReader(Implementation.class);
		Assert.assertNotNull(reader2);
		dslJson.registerReader(Interface.class, dslJson.tryFindReader(Implementation.class));
		JsonReader.ReadObject<?> reader3 = dslJson.tryFindReader(Interface.class);
		Assert.assertNotNull(reader3);
	}

	@Test
	public void skipEscaped1() throws IOException {
		DslJson<Object> dslJson = new DslJson<Object>();
		byte[] input = "{\"a\":1,\"b\":\"\\\",\"c\":\"\\\\\"}".getBytes("UTF-8");
		JsonReader reader = dslJson.newReader(input);
		Assert.assertEquals('{', reader.getNextToken());
		Assert.assertEquals('"', reader.getNextToken());
		Assert.assertEquals("a", reader.readKey());
		Assert.assertEquals(',', reader.skip());
		Assert.assertEquals('"', reader.getNextToken());
		Assert.assertEquals("b", reader.readKey());
		Assert.assertEquals('c', reader.skip());
	}

	@Test
	public void skipEscaped2() throws IOException {
		DslJson<Object> dslJson = new DslJson<Object>();
		byte[] input = "{\"a\":1,\"b\":\"\\\"\",\"c\":\"\\\\\"}".getBytes("UTF-8");
		JsonReader reader = dslJson.newReader(input);
		Assert.assertEquals('{', reader.getNextToken());
		Assert.assertEquals('"', reader.getNextToken());
		Assert.assertEquals("a", reader.readKey());
		Assert.assertEquals(',', reader.skip());
		Assert.assertEquals('"', reader.getNextToken());
		Assert.assertEquals("b", reader.readKey());
		Assert.assertEquals(',', reader.skip());
	}

	@Test
	public void skipEscaped3() throws IOException {
		DslJson<Object> dslJson = new DslJson<Object>();
		byte[] input = "{\"a\":1,\"b\":\"\\\\\",\"c\":\"\\\\\\\"\",\"d\":\"\\\"abc\"}".getBytes("UTF-8");
		Map<String, Object> map = dslJson.deserialize(Map.class, input, input.length);
		Assert.assertEquals(4, map.size());
		Assert.assertEquals(map.get("a"), 1L);
		Assert.assertEquals(map.get("b"), "\\");
		Assert.assertEquals(map.get("c"), "\\\"");
		JsonReader reader = dslJson.newReader(input);
		Assert.assertEquals('{', reader.getNextToken());
		Assert.assertEquals('"', reader.getNextToken());
		Assert.assertEquals("a", reader.readKey());
		Assert.assertEquals(',', reader.skip());
		Assert.assertEquals('"', reader.getNextToken());
		Assert.assertEquals("b", reader.readKey());
		Assert.assertEquals(',', reader.skip());
		Assert.assertEquals('"', reader.getNextToken());
		Assert.assertEquals("c", reader.readKey());
		Assert.assertEquals(',', reader.skip());
		Assert.assertEquals('"', reader.getNextToken());
		Assert.assertEquals("d", reader.readKey());
		Assert.assertEquals('}', reader.skip());
	}

	@Test
	public void canReadStringAtTheEndOfLongBuffer() throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		sb.append("\"");
		for (int i = 0; i < 10; i++) {
			sb.append("abcdefghijklmnopq");
		}
		sb.append("\"");
		String largeString = sb.toString();
		byte[] bytes = largeString.getBytes();
		DslJson<Object> json = new DslJson<Object>();
		try {
			json.deserialize(String.class, bytes, bytes.length - 1);
			Assert.fail();
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("at: 171"));
		}
	}

	@Test
	public void canReadStringAtTheEndOfShortBuffer() throws IOException, InterruptedException {
		String largeString = "\"abcdefghijklmnopq\"";
		byte[] bytes = largeString.getBytes();
		DslJson<Object> json = new DslJson<Object>();
		try {
			json.deserialize(String.class, bytes, bytes.length - 1);
			Assert.fail();
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("at: 18"));
		}
	}
}
