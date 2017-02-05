package com.dslplatform.json;

import com.dslplatform.json.generated.GA0A0Lc;
import org.junit.Assert;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

public class StreamTest {
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

	@Test
	public void testIteratingLongFromStream() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("[null");
		for (int i = 0; i < 1000; i++) {
			sb.append(",");
			sb.append(Integer.toString(i));
		}
		sb.append("]");
		byte[] bytes = sb.toString().getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		Iterator<Long> result = json.iterateOver(Long.class, is, new byte[512]);
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
			writer.writeAscii("{\"x\":");
			NumberConverter.serialize(x, writer);
			writer.writeAscii(",\"y\":");
			NumberConverter.serialize(y, writer);
			writer.writeAscii(",\"z\":");
			if (z) {
				writer.writeAscii("true}");
			} else {
				writer.writeAscii("false}");
			}
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
		Assert.assertEquals(largeString.substring(1, largeString.length() - 1), result);
	}

	@Test
	public void canReadStringAtTheEndOfBuffer() throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 50; i++) {
			sb.append(" ");
		}
		sb.append("\"");
		for (int i = 0; i < 10; i++) {
			sb.append("abcdefghijklmnopq");
		}
		sb.append("\"");
		String largeString = sb.toString();
		byte[] bytes = largeString.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		String result = json.deserialize(String.class, is, new byte[64]);
		Assert.assertEquals(largeString.trim().substring(1, largeString.trim().length() - 1), result);
	}

	@Test
	public void canReadBase64FromStream() throws IOException, InterruptedException {
		byte[] buf = new byte[8196 * 8];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte) i;
		}
		DslJson<Object> json = new DslJson<Object>();
		JsonWriter writer = json.newWriter();
		writer.writeBinary(buf);
		ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
		byte[] result = json.deserialize(byte[].class, is, new byte[512]);
		Assert.assertArrayEquals(buf, result);
	}

	@Test
	public void willDetectMissingEndOfString() throws IOException, InterruptedException {
		String largeString = "\"abcd";
		byte[] bytes = largeString.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		try {
			json.deserialize(String.class, is, new byte[512]);
			Assert.fail("expecting quote error");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("JSON string was not closed with a double quote at: 5"));
		}
	}

	@Test
	public void willDetectMissingEndOfStringAfterBuffer() throws IOException, InterruptedException {
		String largeString = "\"0123456789012345678901234567890123456789012345678901234567890123456789";
		byte[] bytes = largeString.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		try {
			json.deserialize(String.class, is, new byte[70]);
			Assert.fail("expecting quote error");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("JSON string was not closed with a double quote"));
		}
	}

	@Test
	public void willDetectMissingEndOfStringBufferSize() throws IOException, InterruptedException {
		String largeString = "\"012345678901234567890123456789012345678901234567890123456789012345678";
		byte[] bytes = largeString.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		try {
			json.deserialize(String.class, is, new byte[70]);
			Assert.fail("expecting error");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("JSON string was not closed with a double quote"));
		}
	}

	@Test
	public void willDetectMissingEndOfStringBufferSizePlus() throws IOException, InterruptedException {
		String largeString = "\"01234567890123456789012345678901234567890123456789012345678901234567890";
		byte[] bytes = largeString.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		try {
			json.deserialize(String.class, is, new byte[70]);
			Assert.fail("expecting quote error");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("JSON string was not closed with a double quote"));
		}
	}

	@Test
	public void canRereadInputStream() throws IOException, InterruptedException {
		byte[] buffer = new byte[100];
		for (int i = 0; i < 100; i++) {
			buffer[i] = (byte) (i + 1);
		}
		ByteArrayInputStream is = new ByteArrayInputStream(buffer);
		JsonStreamReader<Object> json = new JsonStreamReader<Object>(is, new byte[64], null);
		Assert.assertEquals(1, json.getNextToken());
		Assert.assertEquals(2, json.getNextToken());
		InputStream reread1 = json.streamFromStart();
		Assert.assertEquals(1, reread1.read());
		Assert.assertEquals(2, reread1.read());
		byte[] tmp = new byte[2];
		int total = reread1.read(tmp);
		Assert.assertEquals(2, total);
		Assert.assertEquals(3, tmp[0]);
		Assert.assertEquals(4, tmp[1]);
		total = reread1.read(new byte[58]);
		Assert.assertEquals(58, total);
		total = reread1.read(tmp);
		Assert.assertEquals(2, total);
		Assert.assertEquals(63, tmp[0]);
		Assert.assertEquals(64, tmp[1]);
		total = reread1.read(tmp);
		Assert.assertEquals(2, total);
		Assert.assertEquals(65, tmp[0]);
		Assert.assertEquals(66, tmp[1]);
	}

	@Test
	public void iterateToOutputObject() throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		sb.append("[{\"x\":1,\"y\":1.1,\"z\":true}");
		for (int i = 0; i < 1000; i++) {
			sb.append(",{\"x\":");
			sb.append(Integer.toString(i));
			sb.append(",\"y\":");
			sb.append(Double.toString(i / 10d));
			sb.append(",\"z\":");
			sb.append(i % 2 == 0 ? "true}" : "false}");
			sb.append(",null");
		}
		sb.append("]");
		byte[] bytes = sb.toString().getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		Iterator<Obj> result = json.iterateOver(Obj.class, is, new byte[512]);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		json.iterateOver(result, os, json.newWriter());
		Assert.assertArrayEquals(os.toByteArray(), bytes);
		os.reset();
		is.reset();
		result = json.iterateOver(Obj.class, is, new byte[512]);
		json.iterateOver(result, Obj.class, os, json.newWriter());
		Assert.assertArrayEquals(os.toByteArray(), bytes);
	}

	@Test
	public void iterateToOutputMap() throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		sb.append("[{\"x\":1,\"y\":1.1,\"z\":true}");
		for (int i = 0; i < 1000; i++) {
			sb.append(",{\"x\":");
			sb.append(Integer.toString(i));
			sb.append(",\"y\":");
			sb.append(Double.toString(i / 10d));
			sb.append(",\"z\":");
			sb.append(i % 2 == 0 ? "true}" : "false}");
			sb.append(",null");
		}
		sb.append("]");
		byte[] bytes = sb.toString().getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		Iterator<Map> result = json.iterateOver(Map.class, is, new byte[512]);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		json.iterateOver(result, os, json.newWriter());
		Assert.assertArrayEquals(os.toByteArray(), bytes);
		os.reset();
		is.reset();
		result = json.iterateOver(Map.class, is, new byte[512]);
		json.iterateOver(result, Map.class, os, json.newWriter());
		Assert.assertArrayEquals(os.toByteArray(), bytes);
	}

	@Test
	public void iterateToOutputLong() throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		sb.append("[-1");
		for (int i = 0; i < 1000; i++) {
			sb.append(",");
			sb.append(Integer.toString(i));
		}
		sb.append("]");
		byte[] bytes = sb.toString().getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		Iterator<Long> result = json.iterateOver(Long.class, is, new byte[512]);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		json.iterateOver(result, os, json.newWriter());
		Assert.assertArrayEquals(os.toByteArray(), bytes);
		os.reset();
		is.reset();
		result = json.iterateOver(Long.class, is, new byte[512]);
		json.iterateOver(result, Long.class, os, json.newWriter());
		Assert.assertArrayEquals(os.toByteArray(), bytes);
	}

	@Test
	public void streamLocationIssue() throws IOException {
		DslJson<Object> dslJson = new DslJson<Object>();
		String json = "{\"URI\":\"a94e4da8-0d14-48f2-97b3-045359aafad5\",\"ID\":\"a94e4da8-0d14-48f2-97b3-045359aafad5\",\"gE0A0Lc\":{\"URI\":\"cb172e84-60dd-4d17-b80f-372e5452fc9f\",\"p0A0Lc\":[null,{\"X\":0.0,\"Y\":0.0},{\"X\":-2.147483648E9,\"Y\":2.147483647E9},{\"X\":-1.0E9,\"Y\":1.0E9},{\"X\":1.401298464324817E-45,\"Y\":3.4028234663852886E38},{\"X\":-1.0000001192092896,\"Y\":1.0000001192092896},{\"X\":-2.000000000000345,\"Y\":1.000000000000234}],\"GA0A0LcID\":\"cb172e84-60dd-4d17-b80f-372e5452fc9f\"}}";
		byte[] bytes = json.getBytes("UTF-8");
		byte[] buffer = new byte[8192];
		GA0A0Lc deser = dslJson.deserialize(GA0A0Lc.class, new ByteArrayInputStream(bytes), buffer);

		Point2D[] points = deser.getGE0A0Lc().getP0A0Lc();
		Assert.assertEquals(7, points.length);
		Assert.assertEquals(1.000000000000234d, points[6].getY(), 0);
	}

	public static class JsonPoint implements JsonObject {

		public double y;

		public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
			sw.writeAscii("{\"y\":");
			com.dslplatform.json.NumberConverter.serialize(y, sw);
			sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		}

		public static final com.dslplatform.json.JsonReader.ReadJsonObject<JsonPoint> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<JsonPoint>() {
			@SuppressWarnings("unchecked")
			@Override
			public JsonPoint deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
				return new JsonPoint(reader);
			}
		};

		private JsonPoint(final com.dslplatform.json.JsonReader<Object> reader) throws java.io.IOException {

			double _y_ = 0;
			byte nextToken = reader.last();
			if (nextToken != '}') {
				int nameHash = reader.fillName();
				nextToken = reader.getNextToken();
				if (nextToken == 'n') {
					if (reader.wasNull()) {
						nextToken = reader.getNextToken();
					} else {
						throw new java.io.IOException("Expecting 'u' (as null) at position " + reader.positionInStream() + ". Found " + (char) nextToken);
					}
				} else {
					switch (nameHash) {

						case -66302220:
							_y_ = NumberConverter.deserializeDouble(reader);
							nextToken = reader.getNextToken();
							break;
						default:
							nextToken = reader.skip();
							break;
					}
				}
				while (nextToken == ',') {
					nextToken = reader.getNextToken();
					nameHash = reader.fillName();
					nextToken = reader.getNextToken();
					if (nextToken == 'n') {
						if (reader.wasNull()) {
							nextToken = reader.getNextToken();
							continue;
						} else {
							throw new java.io.IOException("Expecting 'u' (as null) at position " + reader.positionInStream() + ". Found " + (char) nextToken);
						}
					}
					switch (nameHash) {

						case -66302220:
							_y_ = NumberConverter.deserializeDouble(reader);
							nextToken = reader.getNextToken();
							break;
						default:
							nextToken = reader.skip();
							break;
					}
				}
				if (nextToken != '}') {
					throw new java.io.IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
				}
			}

			this.y = _y_;
		}
	}

	@Test
	public void manualReadWithIterator() throws IOException, InterruptedException {
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
		JsonStreamReader reader = json.newReader(is, new byte[1024]);
		reader.getNextToken(); // {
		reader.getNextToken(); // "
		reader.readKey(); // coordinates
		reader.getNextToken(); // start
		Iterator<JsonPoint> iterator = reader.iterateOver(JsonPoint.JSON_READER);
		double y = 0;
		while (iterator.hasNext()) {
			JsonPoint obj = iterator.next();
			y += obj.y;
		}
		Assert.assertEquals(725.1708016933293d, y, 0);
	}

	@Test
	public void canProcessStreamMultipleTimes() throws IOException, InterruptedException {
		BigDecimal bd = new BigDecimal("01234567890123456789012345678901234567890123456789012345678901234567890");
		String largeString = "\"" + bd + "\"";
		byte[] bytes = largeString.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		DslJson<Object> json = new DslJson<Object>();
		JsonStreamReader<Object> input = json.newReader(is, new byte[1024]);
		JsonReader.ReadObject<BigDecimal> converter = json.tryFindReader(BigDecimal.class);
		for (int i = 0; i < 10; i++) {
			BigDecimal value = json.deserialize(converter, input);
			Assert.assertEquals(bd, value);
			is.reset();
			input.reset(is);
		}
	}
}
