package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class NumberConverterTest {
	@Test
	public void testSerialization() {
		// setup
		final JsonWriter sw = new JsonWriter(40);

		final int from = -1000000;
		final int to = 1000000;
		final long range = to - from + 1;

		for (long value = from; value <= to; value++) {

			// init
			sw.reset();

			// serialization
			NumberConverter.serialize(value, sw);

			// check
			final String valueString = sw.toString();
			final int valueParsed = Integer.valueOf(valueString);
			Assert.assertEquals(value, valueParsed);
		}
	}


	@Test
	public void testPowersOf10() throws IOException {
		String sciForm = "1";

		final int maxLen = Long.toString(Long.MAX_VALUE).length();
		for (int i = 0; i < maxLen; i ++) {
			// space to prevent end of stream gotcha
			final byte[] body = (sciForm + " ").getBytes(Charset.forName("ISO-8859-1"));

			final JsonReader jr = new JsonReader(body, null);
			jr.getNextToken();
			final long parsed = NumberConverter.deserializeLong(jr);

			final long check = Long.valueOf(sciForm);
			Assert.assertEquals(check, parsed);

			sciForm += '0';
		}
	}

	@Test
	public void testGenericNumber() throws IOException {
		String input = "{\"coordinates\": [{\n" +
				"      \"x\": 0.7497682823992804,\n" +
				"      \"y\": 0.11430576315631691,\n" +
				"      \"z\": 0.8336834710515213,\n" +
				"      \"id\": \"1804\",\n" +
				"      \"conf\": {\"1\": [1,true]}\n" +
				"    },\n" +
				"    {\n" +
				"      \"x\": 0.996765457871507,\n" +
				"      \"y\": 0.7250564959301626,\n" +
				"      \"z\": 0.4599639911379607,\n" +
				"      \"id\": \"2546\",\n" +
				"      \"conf\": {\"1\": [1,true]\n" +
				"      }\n" +
				"    }]}";
		DslJson json = new DslJson();
		Map result = (Map) json.deserialize(Map.class, input.getBytes(), input.length());
		Assert.assertNotNull(result);
	}
}
