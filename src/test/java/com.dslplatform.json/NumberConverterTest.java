package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

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
}
