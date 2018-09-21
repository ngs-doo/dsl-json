package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PrettifyTest {
	@Test
	public void number() throws IOException {
		testPrettify(" 123.4 ", "123.4");
	}

	@Test
	public void string() throws IOException {
		testPrettify("\n\"1234\"   ", "\"1234\"");
	}

	@Test
	public void specialStrings() throws IOException {
		testPrettify("[\"1\\2\\\"34\",\"\\\\\",\"\"]", "[\n  \"1\\2\\\"34\",\n  \"\\\\\",\n  \"\"\n]");
	}

	@Test
	public void nullConstant() throws IOException {
		testPrettify("null", "null");
	}

	@Test
	public void trueConstant() throws IOException {
		testPrettify("true", "true");
	}

	@Test
	public void falseConstant() throws IOException {
		testPrettify("false", "false");
	}

	@Test
	public void objectInArray() throws IOException {
		testPrettify("[{\"abc\":123},{\"abc\":234}]",
				"[\n  {\n    \"abc\": 123\n  },\n  {\n    \"abc\": 234\n  }\n]");
	}

	@Test
	public void stuffInArray() throws IOException {
		testPrettify("[true,false,null,{\"abc\":[]},{\"abc\":234}]",
				"[\n  true,\n  false,\n  null,\n  {\n    \"abc\": []\n  },\n  {\n    \"abc\": 234\n  }\n]");
	}

	@Test
	public void stuffInObject() throws IOException {
		testPrettify("{\"a\":true,\"b\":false,\"c\":null,\"d\":{\"abc\":[]},\"e\":{\"abc\":234}}",
				"{\n  \"a\": true,\n  \"b\": false,\n  \"c\": null,\n  \"d\": {\n    \"abc\": []\n  },\n  \"e\": {\n    \"abc\": 234\n  }\n}");
	}

	@Test
	public void emptyObjectInArray() throws IOException {
		testPrettify("[{}]", "[\n  {}\n]");
	}

	@Test
	public void largeIndent() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrettifyOutputStream prettifyStream = new PrettifyOutputStream(out,
				PrettifyOutputStream.IndentType.TABS, 800);

		prettifyStream.write("[42]".getBytes(StandardCharsets.UTF_8));

		String expected = "[\n" +
				indent('\t', 800) + "42\n" +
				"]";

		Assert.assertEquals(expected, out.toString());
	}

	private String indent(char b, int size) {
		byte[] bytes = new byte[size];
		Arrays.fill(bytes, (byte) b);
		return new String(bytes);
	}

	private void testPrettify(String rawJson, String formattedJson) throws IOException {
		testWriteWholeArray(rawJson, formattedJson);
		testWriteByteByByte(rawJson, formattedJson);
		testWriteByChunks(rawJson, formattedJson);
	}

	private void testWriteWholeArray(String rawJson, String formattedJson) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrettifyOutputStream prettifyStream = new PrettifyOutputStream(out);
		prettifyStream.write(rawJson.getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals(formattedJson, out.toString());
	}

	private void testWriteByteByByte(String rawJson, String formattedJson) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrettifyOutputStream prettifyStream = new PrettifyOutputStream(out);
		for (byte b : rawJson.getBytes(StandardCharsets.UTF_8)) {
			prettifyStream.write(b);
		}
		Assert.assertEquals(formattedJson, out.toString());
	}

	private void testWriteByChunks(String rawJson, String formattedJson) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrettifyOutputStream prettifyStream = new PrettifyOutputStream(out);
		for (byte[] chunk : splitArray(rawJson.getBytes(StandardCharsets.UTF_8), 5)) {
			prettifyStream.write(chunk);
		}
		Assert.assertEquals(formattedJson, out.toString());
	}

	private static byte[][] splitArray(final byte[] array, final int chunkSize) {
		int numOfChunks = (int) Math.ceil((double) array.length / chunkSize);
		byte[][] output = new byte[numOfChunks][];
		for (int i = 0; i < numOfChunks; i++) {
			int start = i * chunkSize;
			int length = Math.min(array.length - start, chunkSize);

			byte[] temp = new byte[length];
			System.arraycopy(array, start, temp, 0, length);
			output[i] = temp;
		}
		return output;
	}
}
