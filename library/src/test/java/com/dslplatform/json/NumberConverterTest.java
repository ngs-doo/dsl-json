package com.dslplatform.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

public class NumberConverterTest {

	@Test
	public void rangeCheckInt() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40);
		final JsonReader<Object> sr = new JsonReader<Object>(sw.getByteBuffer(), null);

		final int from = -10000000;
		final int to = 10000000;

		for (long value = from; value <= to; value += 33) {
			sw.reset();

			// serialization
			NumberConverter.serialize(value, sw);

			sr.reset(sw.size());
			sr.read();

			final long valueParsed = NumberConverter.deserializeLong(sr);
			Assert.assertEquals(value, valueParsed);
		}
	}

	@Test
	public void rangeCheckLong() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40);
		final JsonReader<Object> sr = new JsonReader<Object>(sw.getByteBuffer(), null);

		final int from = -10000000;
		final int to = 10000000;

		for (int value = from; value <= to; value += 33) {
			sw.reset();

			// serialization
			NumberConverter.serialize(value, sw);

			sr.reset(sw.size());
			sr.read();

			final long valueParsed = NumberConverter.deserializeInt(sr);
			Assert.assertEquals(value, valueParsed);
		}
	}

	@Test
	public void rangeCheckDecimal() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40);
		final JsonReader<Object> sr = new JsonReader<Object>(sw.getByteBuffer(), null);

		final int from = -10000000;
		final int to = 10000000;

		for (int value = from; value <= to; value += 33) {
			sw.reset();

			// serialization
			BigDecimal bd = BigDecimal.valueOf(value / 100);
			NumberConverter.serialize(bd, sw);

			sr.reset(sw.size());
			sr.read();

			final BigDecimal valueParsed = NumberConverter.deserializeDecimal(sr);
			Assert.assertEquals(bd, valueParsed);
		}
	}

	@Test
	public void rangeCheckDouble() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40);
		final JsonReader<Object> sr = new JsonReader<Object>(sw.getByteBuffer(), null);

		final int from = -10000000;
		final int to = 10000000;

		for (int value = from; value <= to; value += 33) {
			sw.reset();

			// serialization
			double d = value / 100.0;
			NumberConverter.serialize(d, sw);

			sr.reset(sw.size());
			sr.read();

			final double valueParsed = NumberConverter.deserializeDouble(sr);
			Assert.assertEquals(d, valueParsed, 0);
		}
	}

	@Test
	public void rangeCheckFloat() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40);
		final JsonReader<Object> sr = new JsonReader<Object>(sw.getByteBuffer(), null);

		final int from = -10000000;
		final int to = 10000000;

		for (int value = from; value <= to; value += 33) {
			sw.reset();

			// serialization
			float f = value / 100.0f;
			NumberConverter.serialize(f, sw);

			sr.reset(sw.size());
			sr.read();

			final float valueParsed = NumberConverter.deserializeFloat(sr);
			Assert.assertEquals(f, valueParsed, 0);
		}
	}

	@Test
	public void testSerialization() {
		// setup
		final JsonWriter sw = new JsonWriter(40);

		final int from = -1000000;
		final int to = 1000000;

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
	public void testCollectionSerialization() throws IOException {
		final Random rnd = new Random(1337);
		final List<Long> collection = new ArrayList<Long>();
		for (long i = 1L; i <= 1000000000000000000L; i *= 10) {
			collection.add(i);                                    //  1000000
			collection.add(-i);                                   // -1000000
			for (int r = 0; r < 100; r++) {
				collection.add(Math.abs(rnd.nextLong()) % i);     //   234992
				collection.add(-(Math.abs(rnd.nextLong()) % i)); //  -712919
			}
		}
		collection.add(Long.MIN_VALUE);
		collection.add(Long.MAX_VALUE);

		final Long[] boxes = collection.toArray(new Long[0]);
		final long[] primitives = new long[boxes.length];
		for (int i = 0; i < primitives.length; i++) {
			primitives[i] = boxes[i];
		}

		final String expected;
		{
			final StringBuilder tmp = new StringBuilder("[");
			for (long value : primitives) {
				tmp.append(value).append(',');
			}
			tmp.setLength(tmp.length() - 1);
			tmp.append(']');
			expected = tmp.toString();
		}

		final JsonWriter sw = new JsonWriter();
		NumberConverter.serialize(primitives, sw);
		Assert.assertEquals(expected, sw.toString());

		sw.reset();
		sw.serialize(collection, NumberConverter.LongWriter);
		Assert.assertEquals(expected, sw.toString());

		sw.reset();
		sw.serialize(boxes, NumberConverter.LongWriter);
		Assert.assertEquals(expected, sw.toString());
	}

	@Test
	public void testPowersOf10() throws IOException {
		String sciForm = "1";

		final int maxLen = Long.toString(Long.MAX_VALUE).length();
		for (int i = 0; i < maxLen; i++) {
			// space to prevent end of stream gotcha
			final byte[] body = (sciForm + " ").getBytes(Charset.forName("ISO-8859-1"));

			final JsonReader<Object> jr = new JsonReader<Object>(body, null);
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

	@Test
	public void primitiveIntArrDeser() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter();

		final int[] input = new int[60000];
		for (int i = 0; i < input.length; i++) {
			input[i] = i;
		}

		NumberConverter.serialize(input, sw);
		final JsonReader<Object> sr = new JsonReader<Object>(sw.getByteBuffer(), sw.size());

		// init
		sr.reset(sw.size());
		sr.read();
		sr.read();

		int[] numbers = NumberConverter.deserializeIntArray(sr);
		Assert.assertArrayEquals(input, numbers);
	}

	@Test
	public void primitiveLongArrDeser() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter();

		final long[] input = new long[60000];
		for (int i = 0; i < input.length; i++) {
			input[i] = i;
		}

		NumberConverter.serialize(input, sw);
		final JsonReader<Object> sr = new JsonReader<Object>(sw.getByteBuffer(), sw.size());

		// init
		sr.reset(sw.size());
		sr.read();
		sr.read();

		long[] numbers = NumberConverter.deserializeLongArray(sr);
		Assert.assertArrayEquals(input, numbers);
	}

	@Test
	public void primitiveFloatArrDeser() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter();

		final float[] input = new float[60000];
		for (int i = 0; i < input.length; i++) {
			input[i] = 1.0f * i;
		}

		NumberConverter.serialize(input, sw);
		final JsonReader<Object> sr = new JsonReader<Object>(sw.getByteBuffer(), sw.size());

		// init
		sr.reset(sw.size());
		sr.read();
		sr.read();

		float[] numbers = NumberConverter.deserializeFloatArray(sr);
		Assert.assertArrayEquals(input, numbers, 0);
	}

	@Test
	public void primitiveDoubleArrDeser() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter();

		final double[] input = new double[60000];
		for (int i = 0; i < input.length; i++) {
			input[i] = i;
		}

		NumberConverter.serialize(input, sw);
		final JsonReader<Object> sr = new JsonReader<Object>(sw.getByteBuffer(), sw.size());

		// init
		sr.reset(sw.size());
		sr.read();
		sr.read();

		double[] numbers = NumberConverter.deserializeDoubleArray(sr);
		Assert.assertArrayEquals(input, numbers, 0);
	}

	@Test
	public void shortWhitespaceGuard() throws IOException {
		String input = "1234  ";
		JsonReader reader = new JsonReader(input.getBytes(), null);
		reader.getNextToken();
		Number number = NumberConverter.deserializeNumber(reader);
		Assert.assertTrue(number instanceof Long);
	}

	@Test
	public void longWhitespaceGuard() throws IOException {
		String input = "1234        \t\n\r               ";
		JsonReader reader = new JsonReader(input.getBytes(), null);
		reader.getNextToken();
		Number number = NumberConverter.deserializeNumber(reader);
		Assert.assertTrue(number instanceof Long);
	}
}
