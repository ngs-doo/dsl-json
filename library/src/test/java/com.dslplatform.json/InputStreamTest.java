package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class InputStreamTest {
	@Test
	public void testIteratingMapFromStream() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("[{\"x\":1,\"y\":1.1,\"z\":true}");
		for (int i = 0; i < 1000; i++) {
			sb.append(",{\"x\":");
			sb.append(Integer.toString(i));
			sb.append(",\"y\":");
			sb.append(Double.toString(i / 10d));
			sb.append(",\"z\":");
			sb.append(i % 2 == 0 ? "true}" : "false}");
		}
		sb.append("]");
		byte[] bytes = sb.toString().getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		Iterator<Map> result = json.iterateOver(Map.class, is, new byte[512]);
		int total = 0;
		while (result.hasNext()) {
			result.next();
			total++;
		}
		Assert.assertEquals(1001, total);
	}

	static class Obj implements JsonObject {

		public int x;
		public double y;
		public boolean z;

		@Override
		public void serialize(JsonWriter writer, boolean minimal) {
		}

		public static final JsonReader.ReadJsonObject<Obj> JSON_READER = new JsonReader.ReadJsonObject<Obj>() {
			@Override
			public Obj deserialize(JsonReader reader) throws IOException {
				return deserializeReader(reader);
			}
		};

		public static Obj deserializeReader(JsonReader rdr) throws IOException {
			Obj obj = new Obj();
			rdr.fillName();
			rdr.getNextToken();
			obj.x = NumberConverter.deserializeInt(rdr);
			rdr.getNextToken();
			rdr.getNextToken();
			rdr.fillName();
			rdr.getNextToken();
			obj.y = NumberConverter.deserializeDouble(rdr);
			rdr.getNextToken();
			rdr.getNextToken();
			rdr.fillName();
			rdr.getNextToken();
			obj.z = BoolConverter.deserialize(rdr);
			rdr.getNextToken();
			return obj;
		}
	}

	@Test
	public void testIteratingJsonObjectFromStream() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("[{\"x\":1,\"y\":1.1,\"z\":true}");
		for (int i = 0; i < 1000; i++) {
			sb.append(",{\"x\":");
			sb.append(Integer.toString(i));
			sb.append(",\"y\":");
			sb.append(Double.toString(i / 10d));
			sb.append(",\"z\":");
			sb.append(i % 2 == 0 ? "true}" : "false}");
		}
		sb.append("]");
		byte[] bytes = sb.toString().getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		Iterator<Obj> result = json.iterateOver(Obj.class, is, new byte[512]);
		int total = 0;
		while (result.hasNext()) {
			result.next();
			total++;
		}
		Assert.assertEquals(1001, total);
	}

	@Test
	public void readObjectFromStream() throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"coordinates\": [{\n" +
				"      \"x\": 0.7497682823992804,\n" +
				"      \"y\": 0.11430576315631691,\n" +
				"      \"z\": 0.8336834710515213,\n" +
				"      \"id\": \"1804\",\n" +
				"      \"conf\": {\"1\": [1,true]}\n" +
				"    }\n");
		for (int i = 0; i < 1000; i++) {
			sb.append(",{\n" +
					"      \"x\": 0.996765457871507,\n" +
					"      \"y\": 0.7250564959301626,\n" +
					"      \"z\": 0.4599639911379607,\n" +
					"      \"id\": \"2546\",\n" +
					"      \"conf\": {\"1\": [1,true]\n" +
					"      }\n" +
					"    }");
		}
		sb.append("]}");
		byte[] bytes = sb.toString().getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		Map result = json.deserialize(Map.class, is, new byte[1024]);
		Assert.assertNotNull(result);
	}

	@Test
	public void canReadStringLargerThanBuffer() throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		sb.append("\"");
		for (int i = 0; i < 1000; i++) {
			sb.append("abcdefghijklmnopq");
		}
		sb.append("\"");
		String largeString = sb.toString();
		byte[] bytes = largeString.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		String result = json.deserialize(String.class, is, new byte[512]);
		Assert.assertEquals(largeString.substring(1 , largeString.length() - 1), result);
	}
}
