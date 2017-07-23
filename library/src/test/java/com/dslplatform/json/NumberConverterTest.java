package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

public class NumberConverterTest {

	@Test
	public void rangeCheckInt() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = new JsonReader<Object>(sw.getByteBuffer(), null);
		final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(new byte[0]), new byte[64], null);

		final int from = -10000000;
		final int to = 10000000;

		for (long value = from; value <= to; value += 33) {
			sw.reset();

			// serialization
			NumberConverter.serialize(value, sw);

			jr.reset(sw.size());
			jr.read();

			final long valueParsed1 = NumberConverter.deserializeLong(jr);
			Assert.assertEquals(value, valueParsed1);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.reset(is);
			jsr.read();

			final long valueParsed2 = NumberConverter.deserializeLong(jsr);
			Assert.assertEquals(value, valueParsed2);
		}
	}

	@Test
	public void rangeCheckLong() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = new JsonReader<Object>(sw.getByteBuffer(), null);
		final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(new byte[0]), new byte[64], null);

		final long from = -10000000000L;
		final long to = 10000000000L;

		for (long value = from; value <= to; value += 33333) {
			sw.reset();

			// serialization
			NumberConverter.serialize(value, sw);

			jr.reset(sw.size());
			jr.read();

			final long valueParsed1 = NumberConverter.deserializeLong(jr);
			Assert.assertEquals(value, valueParsed1);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.reset(is);
			jsr.read();

			final long valueParsed2 = NumberConverter.deserializeLong(jsr);
			Assert.assertEquals(value, valueParsed2);
		}
	}

	@Test
	public void rangeCheckDecimal() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = new JsonReader<Object>(sw.getByteBuffer(), null);
		final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(new byte[0]), new byte[64], null);

		final int from = -100000000;
		final int to = 100000000;

		for (int value = from; value <= to; value += 333) {
			sw.reset();

			// serialization
			BigDecimal bd = BigDecimal.valueOf(value / 100);
			NumberConverter.serialize(bd, sw);

			jr.reset(sw.size());
			jr.read();

			final BigDecimal valueParsed1 = NumberConverter.deserializeDecimal(jr);
			Assert.assertEquals(bd, valueParsed1);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.reset(is);
			jsr.read();

			final BigDecimal valueParsed2 = NumberConverter.deserializeDecimal(jsr);
			Assert.assertEquals(bd, valueParsed2);
		}
	}

	@Test
	public void rangeCheckDouble() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = new JsonReader<Object>(sw.getByteBuffer(), null);
		final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(new byte[0]), new byte[64], null);

		final int from = -10000000;
		final int to = 10000000;
		final double[] dividers = { 1d, 10d, 100d, 1000d, 10000d, 100000d };

		for (int value = from, i = 0; value <= to; value += 33, i++) {
			sw.reset();

			// serialization
			double d = value / dividers[i%dividers.length];
			NumberConverter.serialize(d, sw);

			jr.reset(sw.size());
			jr.read();

			final double valueParsed1 = NumberConverter.deserializeDouble(jr);
			Assert.assertEquals(d, valueParsed1, 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.reset(is);
			jsr.read();

			final double valueParsed2 = NumberConverter.deserializeDouble(jsr);
			Assert.assertEquals(d, valueParsed2, 0);
		}
	}

	@Test
	public void rangeCheckFloat() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = new JsonReader<Object>(sw.getByteBuffer(), null);
		final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(new byte[0]), new byte[64], null);

		final int from = -10000000;
		final int to = 10000000;
		final float[] dividers = { 1f, 10f, 100f, 1000f, 10000f };

		for (int value = from, i = 0; value <= to; value += 33, i++) {
			sw.reset();

			// serialization
			float f = value / dividers[i%dividers.length];
			NumberConverter.serialize(f, sw);

			jr.reset(sw.size());
			jr.read();

			final float valueParsed1 = NumberConverter.deserializeFloat(jr);
			Assert.assertEquals(f, valueParsed1, 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.reset(is);
			jsr.read();

			final float valueParsed2 = NumberConverter.deserializeFloat(jsr);
			Assert.assertEquals(f, valueParsed2, 0);
		}
	}

	@Test
	public void testSerialization() {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);

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

		final JsonWriter sw = new JsonWriter(null);
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
			final long parsed1 = NumberConverter.deserializeLong(jr);
			final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(body), new byte[64], null);
			jsr.getNextToken();
			final long parsed2 = NumberConverter.deserializeLong(jsr);

			final long check = Long.valueOf(sciForm);
			Assert.assertEquals(check, parsed1);
			Assert.assertEquals(check, parsed2);

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
	public void testGenericNumberLongBoundaries() throws IOException {
		final Long maxIntAsLong = Long.valueOf(Integer.MAX_VALUE);
		final Long minIntAsLong = Long.valueOf(Integer.MIN_VALUE);
		final BigDecimal maxIntWithDecimalAsBigDecimal = BigDecimal.valueOf(Integer.MAX_VALUE).setScale(1);
		final BigDecimal minIntWithDecimalAsBigDecimal = BigDecimal.valueOf(Integer.MIN_VALUE).setScale(1);
		final Long positive18DigitLong = Long.valueOf(876543210987654321L);
		final Long negative18DigitLong = Long.valueOf(-876543210987654321L);
		final BigDecimal positive18DigitAndOneDecimal = BigDecimal.valueOf(876543210987654321L).setScale(1);
		final BigDecimal negative18DigitAndOneDecimal  = BigDecimal.valueOf(-876543210987654321L).setScale(1);
		final Long maxLong = Long.valueOf(Long.MAX_VALUE);
		final Long minLong = Long.valueOf(Long.MIN_VALUE);
		final BigDecimal maxLongPlusOneAsBigDecimal = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE);
		final BigDecimal minLongMinusOneAsBigDecimal = BigDecimal.valueOf(Long.MIN_VALUE).subtract(BigDecimal.ONE);

		String input = "{\n" +
				"\"maxIntAsLong\":" +          maxIntAsLong + ",\n" +
				"\"maxIntAsLongWithSign\":+" + maxIntAsLong + ",\n" +
				"\"minIntAsLong\":" +          minIntAsLong + ",\n" +
				"\"maxIntWithDecimalAsBigDecimal\":" +          maxIntWithDecimalAsBigDecimal + ",\n" +
				"\"maxIntWithDecimalAsBigDecimalWithSign\":+" + maxIntWithDecimalAsBigDecimal + ",\n" +
				"\"minIntWithDecimalAsBigDecimal\":" +          minIntWithDecimalAsBigDecimal + ",\n" +
				"\"positive18DigitLong\":" +          positive18DigitLong + ",\n" +
				"\"positive18DigitLongWithSign\":+" + positive18DigitLong + ",\n" +
				"\"negative18DigitLong\":" +          negative18DigitLong + ",\n" +
				"\"positive18DigitAndOneDecimal\":" +          positive18DigitAndOneDecimal + ",\n" +
				"\"positive18DigitAndOneDecimalWithSign\":+" + positive18DigitAndOneDecimal + ",\n" +
				"\"negative18DigitAndOneDecimal\":" +          negative18DigitAndOneDecimal + ",\n" +
				"\"maxLong\":" +          maxLong + ",\n" +
				"\"maxLongWithSign\":+" + maxLong + ",\n" +
				"\"minLong\":" +          minLong + ",\n" +
				"\"maxLongPlusOneAsBigDecimal\":" +          maxLongPlusOneAsBigDecimal + ",\n" +
				"\"maxLongPlusOneAsBigDecimalWithSign\":+" + maxLongPlusOneAsBigDecimal + ",\n" +
				"\"minLongMinusOneAsBigDecimal\":" +         minLongMinusOneAsBigDecimal + "\n" +
		"}";

		DslJson json = new DslJson();
		Map result = (Map) json.deserialize(Map.class, input.getBytes("UTF-8"), input.length());
		Assert.assertEquals(maxIntAsLong, result.get("maxIntAsLong"));
		Assert.assertEquals(maxIntAsLong, result.get("maxIntAsLongWithSign"));
		Assert.assertEquals(minIntAsLong, result.get("minIntAsLong"));
 		Assert.assertEquals(maxIntWithDecimalAsBigDecimal, result.get("maxIntWithDecimalAsBigDecimal"));
		Assert.assertEquals(maxIntWithDecimalAsBigDecimal, result.get("maxIntWithDecimalAsBigDecimalWithSign"));
		Assert.assertEquals(minIntWithDecimalAsBigDecimal, result.get("minIntWithDecimalAsBigDecimal"));
		Assert.assertEquals(positive18DigitLong, result.get("positive18DigitLong"));
		Assert.assertEquals(positive18DigitLong, result.get("positive18DigitLongWithSign"));
		Assert.assertEquals(negative18DigitLong, result.get("negative18DigitLong"));
		Assert.assertEquals(positive18DigitAndOneDecimal, result.get("positive18DigitAndOneDecimal"));
		Assert.assertEquals(positive18DigitAndOneDecimal, result.get("positive18DigitAndOneDecimalWithSign"));
		Assert.assertEquals(negative18DigitAndOneDecimal, result.get("negative18DigitAndOneDecimal"));
		Assert.assertEquals(maxLong, result.get("maxLong"));
		Assert.assertEquals(maxLong, result.get("maxLongWithSign"));
		Assert.assertEquals(minLong, result.get("minLong"));
		Assert.assertEquals(maxLongPlusOneAsBigDecimal, result.get("maxLongPlusOneAsBigDecimal"));
		Assert.assertEquals(maxLongPlusOneAsBigDecimal, result.get("maxLongPlusOneAsBigDecimalWithSign"));
		Assert.assertEquals(minLongMinusOneAsBigDecimal, result.get("minLongMinusOneAsBigDecimal"));
	}

	@Test
	public void primitiveIntArrDeser() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(null);

		final int[] input = new int[60000];
		for (int i = 0; i < input.length; i++) {
			input[i] = i;
		}

		NumberConverter.serialize(input, sw);
		final JsonReader<Object> jr = new JsonReader<Object>(sw.getByteBuffer(), sw.size());

		// init
		jr.reset(sw.size());
		jr.read();
		jr.read();

		int[] numbers1 = NumberConverter.deserializeIntArray(jr);
		Assert.assertArrayEquals(input, numbers1);

		final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()), new byte[64], null);
		// init
		jsr.read();
		jsr.read();

		int[] numbers2 = NumberConverter.deserializeIntArray(jsr);
		Assert.assertArrayEquals(input, numbers2);
	}

	@Test
	public void primitiveLongArrDeser() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(null);

		final long[] input = new long[60000];
		for (int i = 0; i < input.length; i++) {
			input[i] = i;
		}

		NumberConverter.serialize(input, sw);
		final JsonReader<Object> jr = new JsonReader<Object>(sw.getByteBuffer(), sw.size());

		// init
		jr.reset(sw.size());
		jr.read();
		jr.read();

		long[] numbers1 = NumberConverter.deserializeLongArray(jr);
		Assert.assertArrayEquals(input, numbers1);

		final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()), new byte[64], null);
		// init
		jsr.read();
		jsr.read();

		long[] numbers2 = NumberConverter.deserializeLongArray(jsr);
		Assert.assertArrayEquals(input, numbers2);
	}

	@Test
	public void primitiveFloatArrDeser() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(null);
		final float[] multiplier = { 1f, 1.11f, 1.111f, 1.1111f, 1.11111f };

		final float[] input = new float[60000];
		for (int i = 0; i < input.length; i++) {
			input[i] = i * multiplier[i%multiplier.length];
		}

		NumberConverter.serialize(input, sw);
		final JsonReader<Object> jr = new JsonReader<Object>(sw.getByteBuffer(), sw.size());

		// init
		jr.reset(sw.size());
		jr.read();
		jr.read();

		float[] numbers1 = NumberConverter.deserializeFloatArray(jr);
		Assert.assertArrayEquals(input, numbers1, 0);

		final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()), new byte[64], null);
		// init
		jsr.read();
		jsr.read();

		float[] numbers2 = NumberConverter.deserializeFloatArray(jsr);
		Assert.assertArrayEquals(input, numbers2, 0);
	}

	@Test
	public void primitiveDoubleArrDeser() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(null);
		final double[] multiplier = { 1d, 1.11d, 1.111d, 1.1111d, 1.11111d, 1.111111d, 1.1111111d };

		final double[] input = new double[60000];
		for (int i = 0; i < input.length; i++) {
			input[i] = i * multiplier[i%multiplier.length];
		}

		NumberConverter.serialize(input, sw);
		final JsonReader<Object> jr = new JsonReader<Object>(sw.getByteBuffer(), sw.size());

		// init
		jr.reset(sw.size());
		jr.read();
		jr.read();

		double[] numbers1 = NumberConverter.deserializeDoubleArray(jr);
		Assert.assertArrayEquals(input, numbers1, 0.00000000001d);

		final JsonStreamReader<Object> jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()), new byte[64], null);
		// init
		jsr.read();
		jsr.read();

		double[] numbers2 = NumberConverter.deserializeDoubleArray(jsr);
		Assert.assertArrayEquals(input, numbers2, 0.00000000001d);
	}

	@Test
	public void shortWhitespaceGuard() throws IOException {
		String input = "1234  ";
		JsonReader reader = new JsonReader(input.getBytes(), null);
		reader.getNextToken();
		Number number = NumberConverter.deserializeNumber(reader);
		Assert.assertTrue(number instanceof Long);
		reader = new JsonStreamReader(new ByteArrayInputStream(input.getBytes()), new byte[64], null);
		reader.getNextToken();
		number = NumberConverter.deserializeNumber(reader);
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

	@Test
	public void overflowDetection() throws IOException {
		String input = "1234567890123456        \t\n\r               ";
		JsonReader reader = new JsonReader(input.getBytes(), null);
		reader.getNextToken();
		try {
			NumberConverter.deserializeInt(reader);
			Assert.fail();
		}catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("Integer overflow"));
		}
		input = "-1234567890123456        \t\n\r               ";
		reader = new JsonReader(input.getBytes(), null);
		reader.getNextToken();
		try {
			NumberConverter.deserializeInt(reader);
			Assert.fail();
		}catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("Integer overflow"));
		}
	}
}
