package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HashNameTest {

	private final DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());

	@Test
	public void attributeNameChecksWithBufferSize() throws IOException {
		for (int bufferSize = 64; bufferSize <= 512; bufferSize++) {
			attributeNameChecksWithBufferSize(bufferSize);
		}
		attributeNameChecksWithBufferSize(4096);
	}

	private void attributeNameChecksWithBufferSize(final int bufferSize) throws IOException {
		final byte[] json = buildJsonWithStatusLabelAtReadLimit(bufferSize);
		JsonReader<Object> reader = dslJson.newReader(new ByteArrayInputStream(json), new byte[bufferSize]);
		reader.getNextToken();
		reader.getNextToken();
		reader.readKey();
		StringConverter.deserialize(reader);
		reader.getNextToken();
		reader.getNextToken();
		int hash = reader.fillNameWeakHash();
		Assert.assertEquals(676, hash);
		Assert.assertEquals("status", reader.getLastName());
		Assert.assertTrue(reader.wasLastName("status"));
		Assert.assertEquals(-1169459217, reader.getLastHash());
	}

	@Test
	public void willParseObjectWhenLabelEndsAtReadLimit() throws IOException {
		final byte[] json = buildJsonWithStatusLabelAtReadLimit(4096);
		NamePojo result = dslJson.deserialize(NamePojo.class, new ByteArrayInputStream(json), new byte[4096]);
		Assert.assertNotNull(result);
		Assert.assertEquals("SOME_UID", result.uid);
		Assert.assertEquals("UNKNOWN", result.status);
	}

	private static byte[] buildJsonWithStatusLabelAtReadLimit(final int bufferSize) {
		final int readLimit = bufferSize - 38;
		StringBuilder sb = new StringBuilder();
		sb.append("{\"uid\":\"SOME_UID\",");
		for (int i = 0; i < readLimit - 26; i++) {
			sb.append(' ');
		}
		sb.append("\"status\"");
		sb.append(" : \"UNKNOWN\",");
		sb.append("\"capabilities\":\"");
		for (int i = 0; i < Math.max(60000, bufferSize * 2); i++) {
			sb.append('b');
		}
		sb.append("\"}");
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}

	@CompiledJson
	public static class NamePojo {
		@JsonAttribute(index = 1)
		public String uid;
		@JsonAttribute(index = 2)
		public String status;
		@JsonAttribute(index = 3)
		public String capabilities;
	}
}
