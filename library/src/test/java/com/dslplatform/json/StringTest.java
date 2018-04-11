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
}
