package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonReaderWriterStringTest {

	private final DslJson<Object> json = new DslJson<>();

	private static String repA(final int n) {
		char[] c = new char[n];
		java.util.Arrays.fill(c, 'a');
		return new String(c);
	}

	private static byte[] docWithText(final String text) {
		return ("{\"text\":\"" + text + "\"}").getBytes(StandardCharsets.UTF_8);
	}

	private static String escapeForJson(final String s) {
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"': sb.append("\\\""); break;
				case '\\': sb.append("\\\\"); break;
				case '\n': sb.append("\\n"); break;
				case '\t': sb.append("\\t"); break;
				case '\r': sb.append("\\r"); break;
				default: sb.append(c);
			}
		}
		return sb.toString();
	}

	private static String textFrom(final byte[] doc) throws Exception {
		Map<?, ?> m = (Map<?, ?>) new DslJson<Object>().deserialize(LinkedHashMap.class, doc, doc.length);
		return (String) m.get("text");
	}

	@Test
	public void readStringCleanAsciiVariousLengths() throws Exception {
		for (int len : new int[]{0, 1, 5, 63, 64, 65, 128, 1000, 5000, 50_000}) {
			StringBuilder sb = new StringBuilder(len);
			for (int i = 0; i < len; i++) sb.append((char) ('a' + (i % 26)));
			String expected = sb.toString();
			byte[] doc = docWithText(expected);
			Assert.assertEquals("len=" + len, expected, textFrom(doc));
		}
	}

	@Test
	public void readStringEscapedAndUnicode() throws Exception {
		String[] samples = {
			"a\"b\\c\nd\te\rfg",
			"caf\u00e9 \u2603 \u4e2d\u6587",
			"mixed \"esc\" \u00fc and \ud83d\ude00 emoji",
			"ends with escape\\",
			"starts with escape\nabc",
			"\u0001control at start",
		};
		for (String s : samples) {
			byte[] doc = docWithText(escapeForJson(s));
			Assert.assertEquals("sample=" + s, s, textFrom(doc));
		}
	}

	@Test
	public void readStringStreamModeMatchesBuffered() throws Exception {
		String longClean = new String(new char[12_345]).replace('\0', 'x');
		String mixed = longClean.substring(0, 5000) + "\u00e9" + longClean.substring(0, 6000) + "\\\"";
		for (String s : new String[]{longClean, mixed}) {
			byte[] doc = docWithText(escapeForJson(s));
			String buffered = textFrom(doc);
			Map<?, ?> m2 = (Map<?, ?>) json.deserialize(LinkedHashMap.class, new ByteArrayInputStream(doc));
			Assert.assertEquals("stream vs buffered", buffered, (String) m2.get("text"));
		}
	}

	@Test
	public void escapeSequenceSpanningStreamBlockBoundary() throws Exception {
		// default reader buffer is 4096; place a backslash exactly at the last byte of the first block
		StringBuilder sb = new StringBuilder("{\"text\":\"");
		while (sb.length() < 4096) sb.append('a');
		String doc = sb.toString() + "\\nrest\"}";
		byte[] bytes = doc.getBytes(StandardCharsets.UTF_8);
		int contentStart = doc.indexOf(':') + 2;
		String expected = repA(doc.indexOf('\\') - contentStart) + "\nrest";

		Assert.assertEquals(expected, textFrom(bytes));
		Map<?, ?> m = (Map<?, ?>) json.deserialize(LinkedHashMap.class, new ByteArrayInputStream(bytes));
		Assert.assertEquals("stream mode", expected, (String) m.get("text"));
	}

	@Test
	public void truncatedEscapeWithReusedReaderThrowsParsingException() throws Exception {
		DslJson<Object> json = new DslJson<>();
		// First parse (stream mode) leaves 'n' at buffer slot 12 of the reader's reusable buffer.
		byte[] priming = "{\"text\":\"Xabncd\"}".getBytes(StandardCharsets.UTF_8);
		Map<?, ?> m1 = (Map<?, ?>) json.deserialize(LinkedHashMap.class, new ByteArrayInputStream(priming));
		Assert.assertEquals("Xabncd", m1.get("text"));

		// Second parse: truncated document (12 bytes) ending with a lone backslash at index 11.
		// process(stream) overwrites buffer slots 0..11; slot 12 still holds the stale 'n'.
		byte[] truncated = "{\"text\":\"ab\\".getBytes(StandardCharsets.UTF_8);
		try {
			json.deserialize(LinkedHashMap.class, new ByteArrayInputStream(truncated));
			Assert.fail("expected ParsingException for truncated input");
		} catch (ParsingException expected) {
		}
	}

	@Test
	public void truncatedEscapeAtStreamBlockBoundaryThrows() throws Exception {
		StringBuilder sb = new StringBuilder("{\"text\":\"");
		while (sb.length() < 4096) sb.append('a');
		sb.append('\\'); // document ends with a lone backslash at the block boundary
		byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

		try {
			json.deserialize(LinkedHashMap.class, new ByteArrayInputStream(bytes));
			Assert.fail("expected ParsingException for truncated escape");
		} catch (ParsingException expected) {
		}
	}

	@Test
	public void utf8SequenceSpanningStreamBlockBoundary() throws Exception {
		StringBuilder sb = new StringBuilder("{\"text\":\"");
		while (sb.length() < 4096) sb.append('a');
		String doc = sb.toString() + "\u00e9rest\"}"; // C3 A9 straddles the boundary
		byte[] bytes = doc.getBytes(StandardCharsets.UTF_8);
		int contentStart = doc.indexOf(':') + 2;
		String expected = repA(doc.length() - contentStart - ("\u00e9rest\"}".length())) + "\u00e9rest";

		Assert.assertEquals(expected, textFrom(bytes));
		Map<?, ?> m = (Map<?, ?>) json.deserialize(LinkedHashMap.class, new ByteArrayInputStream(bytes));
		Assert.assertEquals("stream mode", expected, (String) m.get("text"));
	}

	@Test
	public void writeStringLongClean() throws Exception {
		StringBuilder sb = new StringBuilder(3000);
		for (int i = 0; i < 3000; i++) sb.append((char) ('a' + (i % 26)));
		String value = sb.toString();

		JsonWriter w = json.newWriter();
		w.writeString(value);
		byte[] out = w.toByteArray();
		byte[] expected = ("\"" + value + "\"").getBytes(StandardCharsets.UTF_8);
		Assert.assertArrayEquals(expected, out);
	}

	@Test
	public void writeStringLongWithEscapes() throws Exception {
		StringBuilder sb = new StringBuilder(3000);
		sb.append("a\"b\\c\nd");
		while (sb.length() < 3000) sb.append('x');
		String value = sb.toString();

		JsonWriter w = json.newWriter();
		w.writeString(value);
		byte[] out = w.toByteArray();
		byte[] expected = ("\"" + escapeForJson(value) + "\"").getBytes(StandardCharsets.UTF_8);
		Assert.assertArrayEquals(expected, out);
	}

	@Test
	public void writeStringShortWithEscapes() throws Exception {
		String value = "short \"quoted\" value";
		JsonWriter w = json.newWriter();
		w.writeString(value);
		byte[] out = w.toByteArray();
		byte[] expected = ("\"" + escapeForJson(value) + "\"").getBytes(StandardCharsets.UTF_8);
		Assert.assertArrayEquals(expected, out);
	}

	@Test
	public void fullRoundTripWithLongStrings() throws Exception {
		StringBuilder sb = new StringBuilder(20_000);
		for (int i = 0; i < 20_000; i++) sb.append((char) ('A' + (i % 26)));
		String longValue = sb.toString();

		Map<String, Object> in = new LinkedHashMap<>();
		in.put("long", longValue);
		in.put("escaped", "a\"b\\c\nd \u00e9");
		JsonWriter w = json.newWriter();
		json.serializeMap(in, w);
		byte[] bytes = w.toByteArray();

		Map<?, ?> out = (Map<?, ?>) json.deserialize(LinkedHashMap.class, bytes, bytes.length);
		Assert.assertEquals(longValue, out.get("long"));
		Assert.assertEquals("a\"b\\c\nd \u00e9", out.get("escaped"));
	}
}
