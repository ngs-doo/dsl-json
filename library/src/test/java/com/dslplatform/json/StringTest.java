package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class StringTest {

	private final DslJson<Object> dslJson = new DslJson<Object>();

	@Test
	public void emojiDecoding() throws IOException {
		String input = "\"Easter :) 💗\"";
		byte[] bytes = input.getBytes("UTF-8");
		JsonReader<Object> reader = dslJson.newReader().process(bytes, bytes.length);
		reader.read();
		Assert.assertEquals("Easter :) 💗", StringConverter.deserialize(reader));
	}

	@Test
	public void escapedStringInALoop() throws IOException {
		String input = "\"RT @SivuNgcaba: \\\"Things we buy to cover up what's inside, coz they made us hate ourselves and love their wealth.\\\"\"";
		String string = "RT @SivuNgcaba: \"Things we buy to cover up what's inside, coz they made us hate ourselves and love their wealth.\"";
		byte[] bytes = input.getBytes("UTF-8");
		JsonReader<Object> reader = dslJson.newReader();
		for(int i = 0; i < 1000; i++) {
			reader.process(bytes, bytes.length).read();
			Assert.assertEquals(string, StringConverter.deserialize(reader));
		}
		reader.reset();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		for(int i = 0; i < 1000; i++) {
			is.reset();
			reader.process(is).read();
			Assert.assertEquals(string, StringConverter.deserialize(reader));
		}
	}

	@Test
	public void firstLimitInALoop() throws IOException {
		String input = "\"some text to hit the limit while parsing the 64 chars 1234567890 \\\"abcd efg\\\"\"";
		String string = "some text to hit the limit while parsing the 64 chars 1234567890 \"abcd efg\"";
		byte[] bytes = input.getBytes("UTF-8");
		JsonReader<Object> reader = dslJson.newReader();
		for(int i = 0; i < 1000; i++) {
			reader.process(bytes, bytes.length).read();
			Assert.assertEquals(string, StringConverter.deserialize(reader));
		}
		reader.reset();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		for(int i = 0; i < 1000; i++) {
			is.reset();
			reader.process(is).read();
			Assert.assertEquals(string, StringConverter.deserialize(reader));
		}
	}
}
