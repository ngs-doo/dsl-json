package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

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
		byte[] bytes = input.getBytes("UTF-8");
		JsonReader<Object> reader = dslJson.newReader();
		for(int i = 0; i < 1000; i++) {
			reader.process(bytes, bytes.length);
			reader.read();
			Assert.assertEquals("RT @SivuNgcaba: \"Things we buy to cover up what's inside, coz they made us hate ourselves and love their wealth.\"", StringConverter.deserialize(reader));
		}
	}
}
