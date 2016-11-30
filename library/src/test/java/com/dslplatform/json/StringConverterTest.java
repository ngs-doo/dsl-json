package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class StringConverterTest {
	@Test
	public void testCharacterParsing() throws IOException {
		// setup
		final int from = Character.MIN_VALUE;
		final int to = Character.MAX_VALUE;

		for (long value = from; value <= to; value++) {

			// The Unicode standard permanently reserves these code point values for UTF-16 encoding of the high and low surrogates,
			// and they will never be assigned a character, so there should be no reason to encode them.
			if (value >= Character.MIN_SURROGATE && value <= Character.MAX_SURROGATE) continue;

			// Do not test BOM markers (unicode non-characters)
			if (value == 0xfffe || value == 0xffff) continue;

			// init
			final char ch = (char) value;

			final String escaped =
					ch == '\b' ? "\b" :
					ch == '\t' ? "\t" :
					ch == '\n' ? "\n" :
					ch == '\f' ? "\f" :
					ch == '\r' ? "\r" :
					ch == '\\' ? "\\\\" :
					ch == '"' ? "\\\"" : String.valueOf(ch);

			final String replaced =
					ch == '\b' ? "\\b" :
					ch == '\t' ? "\\t" :
					ch == '\n' ? "\\n" :
					ch == '\f' ? "\\f" :
					ch == '\r' ? "\\r" :
					ch == '/' ? "\\/" :
					ch == '\\' ? "\\\\" :
					ch == '"' ? "\\\"" : String.valueOf(ch);

			// Tests all four possible representations of a character, e.g.:
			// "codepoint [47] == escaped [/] == replaced [\/] == unicode (lower) [\u002f] == unicode (upper) [\u002F]"
			// "codepoint [92] == escaped [\\] == replaced [\\] == unicode (lower) [\u005c] == unicode (upper) [\u005C]"
			final String text = String.format(
					"\"codepoint [%d] == escaped [%s] == replaced [%s] == unicode (lower) [\\u%1$04x] == unicode (upper) [\\u%1$04X]\"",
					value,
					escaped,
					replaced);

			// deserialization
			final byte[] buf = text.getBytes("UTF-8");
			final JsonReader<Object> jr = new JsonReader<Object>(buf, null);
			Assert.assertEquals(jr.read(), '"');
			final String read = jr.readString();
			Assert.assertEquals(buf.length, jr.getCurrentIndex()); // test for end of stream

			// check without unicode escapes or replacements, as they will not be present in the result string
			final String expected = String.format(
					"codepoint [%d] == escaped [%s] == replaced [%2$s] == unicode (lower) [%2$s] == unicode (upper) [%2$s]",
					value,
					ch);

			Assert.assertEquals(expected, read);
		}
	}

	@Test
	public void testCharacterPrinting() throws IOException {
		// setup
		final byte[] buf = new byte[1024];
		final JsonWriter jw = new JsonWriter(buf, null);

		final int from = 0;
		final int to = Character.MAX_VALUE;

		for (long value = from; value <= to; value++) {

			// The Unicode standard permanently reserves these code point values for UTF-16 encoding of the high and low surrogates,
			// and they will never be assigned a character, so there should be no reason to encode them.
			if (value >= Character.MIN_SURROGATE && value <= Character.MAX_SURROGATE) continue;

			// Do not test BOM markers (unicode non-characters)
			if (value == 0xfffe || value == 0xffff) continue;

			// init
			final char ch = (char) value;

			final String text = String.format(
					"codepoint [%d] == replaced [%c]",
					value,
					ch);

			// serialization
			jw.reset();
			jw.writeString(text);

			// check
			final String read = new String(buf, 0, jw.size(), "UTF-8");

			// solidus will not be escaped "/"
			// characters < 32 will be unicode escaped "\\u00.."
			final String replaced =
					ch == '\b' ? "\\b" :
					ch == '\t' ? "\\t" :
					ch == '\n' ? "\\n" :
					ch == '\f' ? "\\f" :
					ch == '\r' ? "\\r" :
					ch == '\\' ? "\\\\" :
					ch == '"' ? "\\\"" :
					ch < ' ' ? String.format("\\u%04X", value) : String.valueOf(ch);

			final String expected = String.format(
					"\"codepoint [%d] == replaced [%s]\"",
					value,
					replaced);

			Assert.assertEquals(expected, read);
		}
	}
}
