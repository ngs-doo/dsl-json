package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

public class NumberConverterTest {

	private final DslJson<Object> dslJson = new DslJson<Object>(new DslJson.Settings<Object>().doublePrecision(JsonReader.DoublePrecision.HIGH));

	@Test
	public void rangeCheckInt() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final int from = -10000000;
		final int to = 10000000;

		for (int value = from; value <= to; value += 33) {
			sw.reset();

			// serialization
			NumberConverter.serialize(value, sw);

			jr.process(null, sw.size());
			jr.read();
			final int valueParsed1 = NumberConverter.deserializeInt(jr);
			Assert.assertEquals(value, valueParsed1);

			jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
			jsr.read();
			final int valueParsed2 = NumberConverter.deserializeInt(jsr);
			Assert.assertEquals(value, valueParsed2);
		}
	}

	@Test
	public void rangeCheckLong() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final long from = -10000000000L;
		final long to = 10000000000L;

		for (long value = from; value <= to; value += 33333) {
			sw.reset();

			// serialization
			NumberConverter.serialize(value, sw);

			jr.process(null, sw.size());
			jr.read();
			final long valueParsed1 = NumberConverter.deserializeLong(jr);
			Assert.assertEquals(value, valueParsed1);

			jr.process(null, sw.size());
			jr.read();
			final Number numberParsed1 = NumberConverter.deserializeNumber(jr);
			Assert.assertEquals(value, numberParsed1);

			jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
			jsr.read();
			final long valueParsed2 = NumberConverter.deserializeLong(jsr);
			Assert.assertEquals(value, valueParsed2);

			jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
			jsr.read();
			final Number numberParsed2 = NumberConverter.deserializeNumber(jsr);
			Assert.assertEquals(value, numberParsed2);
		}
	}

	@Test
	public void rangeCheckDecimal() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final int from = -100000000;
		final int to = 100000000;
		final double[] dividers = {1, 10, 0.1, 100, 0.01, 1000, 0.001 };
		int x = 0;

		for (int value = from; value <= to; value += 333) {
			sw.reset();

			// serialization
			BigDecimal bd = BigDecimal.valueOf(value / dividers[x++%dividers.length]);
			NumberConverter.serialize(bd, sw);

			jr.process(null, sw.size());
			jr.read();
			final BigDecimal valueParsed1 = NumberConverter.deserializeDecimal(jr);
			Assert.assertEquals(bd, valueParsed1);
			jr.process(null, sw.size());
			jr.read();
			final Number numberParsed1 = NumberConverter.deserializeNumber(jr);
			Assert.assertEquals(bd, BigDecimal.valueOf(numberParsed1.doubleValue()));

			jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
			jsr.read();
			final BigDecimal valueParsed2 = NumberConverter.deserializeDecimal(jsr);
			Assert.assertEquals(bd, valueParsed2);
			jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
			jsr.read();
			final Number numberParsed2 = NumberConverter.deserializeNumber(jsr);
			Assert.assertEquals(bd, BigDecimal.valueOf(numberParsed2.doubleValue()));
		}
	}

	@Test
	public void rangeCheckDouble() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final int from = -10000000;
		final int to = 10000000;
		final double[] dividers = { 1d, 10d, 100d, 1000d, 10000d, 100000d };

		for (int value = from, i = 0; value <= to; value += 33, i++) {
			sw.reset();

			// serialization
			double d = value / dividers[i%dividers.length];
			NumberConverter.serialize(d, sw);

			jr.process(null, sw.size());
			jr.read();
			final double valueParsed1 = NumberConverter.deserializeDouble(jr);
			Assert.assertEquals(d, valueParsed1, 0);
			jr.process(null, sw.size());
			jr.read();
			final Number numberParsed1 = NumberConverter.deserializeNumber(jr);
			Assert.assertEquals(d, numberParsed1.doubleValue(), 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
			jsr.read();

			final double valueParsed2 = NumberConverter.deserializeDouble(jsr);
			Assert.assertEquals(d, valueParsed2, 0);
			jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
			jsr.read();
			final Number numberParsed2 = NumberConverter.deserializeNumber(jsr);
			Assert.assertEquals(d, numberParsed2.doubleValue(), 0);
		}
	}

	@Test
	public void rangeCheckFloat() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final int from = -10000000;
		final int to = 10000000;
		final float[] dividers = { 1f, 10f, 100f, 1000f, 10000f };

		for (int value = from, i = 0; value <= to; value += 33, i++) {
			sw.reset();

			// serialization
			float f = value / dividers[i%dividers.length];
			NumberConverter.serialize(f, sw);

			jr.process(null, sw.size());
			jr.read();

			final float valueParsed1 = NumberConverter.deserializeFloat(jr);
			Assert.assertEquals(f, valueParsed1, 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
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
	public void testCollectionSerialization() {
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
		sw.serialize(collection, NumberConverter.LONG_WRITER);
		Assert.assertEquals(expected, sw.toString());

		sw.reset();
		sw.serialize(boxes, NumberConverter.LONG_WRITER);
		Assert.assertEquals(expected, sw.toString());
	}

	@Test
	public void testNonRandomAccessCollectionSerialization() {
		final Random rnd = new Random(1337);
		final List<Long> collection = new LinkedList<Long>();
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

		final String expected;
		{
			final StringBuilder tmp = new StringBuilder("[");
			for (long value : collection) {
				tmp.append(value).append(',');
			}
			tmp.setLength(tmp.length() - 1);
			tmp.append(']');
			expected = tmp.toString();
		}

		final JsonWriter sw = new JsonWriter(null);
		sw.serialize(collection, NumberConverter.LONG_WRITER);
		Assert.assertEquals(expected, sw.toString());
	}

	@Test
	public void testPowersOf10() throws IOException {
		String sciForm = "1";

		final int maxLen = Long.toString(Long.MAX_VALUE).length();
		for (int i = 0; i < maxLen; i++) {
			// space to prevent end of stream gotcha
			final byte[] body = (sciForm + " ").getBytes(Charset.forName("ISO-8859-1"));

			final JsonReader<Object> jr = dslJson.newReader(body);
			jr.getNextToken();
			final long parsed1 = NumberConverter.deserializeLong(jr);
			jr.process(new byte[64], 64);
			jr.process(new ByteArrayInputStream(body));
			jr.getNextToken();
			final long parsed2 = NumberConverter.deserializeLong(jr);

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
		final Long positive18DigitLong = 876543210987654321L;
		final Long negative18DigitLong = -876543210987654321L;
		final BigDecimal positive18DigitAndOneDecimal = BigDecimal.valueOf(876543210987654321L).setScale(1);
		final BigDecimal negative18DigitAndOneDecimal  = BigDecimal.valueOf(-876543210987654321L).setScale(1);
		final Long maxLong = Long.MAX_VALUE;
		final Long minLong = Long.MIN_VALUE;
		final BigDecimal maxLongPlusOneAsBigDecimal = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE);
		final BigDecimal minLongMinusOneAsBigDecimal = BigDecimal.valueOf(Long.MIN_VALUE).subtract(BigDecimal.ONE);

		String input = "{\n" +
				"\"maxIntAsLong\":" +          maxIntAsLong + ",\n" +
				"\"minIntAsLong\":" +          minIntAsLong + ",\n" +
				"\"maxIntWithDecimalAsBigDecimal\":" +          maxIntWithDecimalAsBigDecimal + ",\n" +
				"\"minIntWithDecimalAsBigDecimal\":" +          minIntWithDecimalAsBigDecimal + ",\n" +
				"\"positive18DigitLong\":" +          positive18DigitLong + ",\n" +
				"\"negative18DigitLong\":" +          negative18DigitLong + ",\n" +
				"\"positive18DigitAndOneDecimal\":" +          positive18DigitAndOneDecimal + ",\n" +
				"\"negative18DigitAndOneDecimal\":" +          negative18DigitAndOneDecimal + ",\n" +
				"\"maxLong\":" +          maxLong + ",\n" +
				"\"minLong\":" +          minLong + ",\n" +
				"\"maxLongPlusOneAsBigDecimal\":" +          maxLongPlusOneAsBigDecimal + ",\n" +
				"\"minLongMinusOneAsBigDecimal\":" +         minLongMinusOneAsBigDecimal + "\n" +
		"}";

		DslJson json = new DslJson();
		Map result = (Map) json.deserialize(Map.class, input.getBytes("UTF-8"), input.length());
		Assert.assertEquals(maxIntAsLong, result.get("maxIntAsLong"));
		Assert.assertEquals(minIntAsLong, result.get("minIntAsLong"));
 		Assert.assertEquals(maxIntWithDecimalAsBigDecimal, result.get("maxIntWithDecimalAsBigDecimal"));
		Assert.assertEquals(minIntWithDecimalAsBigDecimal, result.get("minIntWithDecimalAsBigDecimal"));
		Assert.assertEquals(positive18DigitLong, result.get("positive18DigitLong"));
		Assert.assertEquals(negative18DigitLong, result.get("negative18DigitLong"));
		Assert.assertEquals(positive18DigitAndOneDecimal, result.get("positive18DigitAndOneDecimal"));
		Assert.assertEquals(negative18DigitAndOneDecimal, result.get("negative18DigitAndOneDecimal"));
		Assert.assertEquals(maxLong, result.get("maxLong"));
		Assert.assertEquals(minLong, result.get("minLong"));
		Assert.assertEquals(maxLongPlusOneAsBigDecimal, result.get("maxLongPlusOneAsBigDecimal"));
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
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		// init
		jr.process(null, sw.size());
		jr.read();
		jr.read();

		int[] numbers1 = NumberConverter.deserializeIntArray(jr);
		Assert.assertArrayEquals(input, numbers1);

		jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
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
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		// init
		jr.process(null, sw.size());
		jr.read();
		jr.read();

		long[] numbers1 = NumberConverter.deserializeLongArray(jr);
		Assert.assertArrayEquals(input, numbers1);

		jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
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
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		// init
		jr.process(null, sw.size());
		jr.read();
		jr.read();

		float[] numbers1 = NumberConverter.deserializeFloatArray(jr);
		Assert.assertArrayEquals(input, numbers1, 0);

		jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
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
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		// init
		jr.process(null, sw.size());
		jr.read();
		jr.read();

		double[] numbers1 = NumberConverter.deserializeDoubleArray(jr);
		Assert.assertArrayEquals(input, numbers1, 0.00000000001d);

		jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
		// init
		jsr.read();
		jsr.read();

		double[] numbers2 = NumberConverter.deserializeDoubleArray(jsr);
		Assert.assertArrayEquals(input, numbers2, 0.00000000001d);
	}

	@Test
	public void shortWhitespaceGuard() throws IOException {
		String input = "1234  ";
		final JsonReader<Object> jr = dslJson.newReader(input.getBytes());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(input.getBytes()), new byte[64]);
		jr.getNextToken();
		Number number = NumberConverter.deserializeNumber(jr);
		Assert.assertTrue(number instanceof Long);
		jsr.getNextToken();
		number = NumberConverter.deserializeNumber(jsr);
		Assert.assertTrue(number instanceof Long);
	}

	@Test
	public void longWhitespaceGuard() throws IOException {
		String input = "1234        \t\n\r               ";
		final JsonReader<Object> reader = dslJson.newReader(input.getBytes());
		reader.getNextToken();
		Number number = NumberConverter.deserializeNumber(reader);
		Assert.assertTrue(number instanceof Long);
	}

	@Test
	public void overflowDetection() throws IOException {
		String input = "1234567890123456        \t\n\r               ";
		JsonReader<Object> reader = dslJson.newReader(input.getBytes());
		reader.getNextToken();
		try {
			NumberConverter.deserializeInt(reader);
			Assert.fail();
		}catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("Integer overflow"));
		}
		input = "-1234567890123456        \t\n\r               ";
		reader = dslJson.newReader(input.getBytes());
		reader.getNextToken();
		try {
			NumberConverter.deserializeInt(reader);
			Assert.fail();
		}catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("Integer overflow"));
		}
	}

	@Test
	public void doubleRandom() throws IOException {
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final Random rnd = new Random(0);

		for (int i = 0; i < 1000000; i++) {
			sw.reset();

			// serialization
			double d = rnd.nextDouble();
			NumberConverter.serialize(d, sw);

			jr.process(null, sw.size());
			jr.read();

			final double valueParsed1 = NumberConverter.deserializeDouble(jr);
			Assert.assertEquals(d, valueParsed1, 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
			jsr.read();

			final double valueParsed2 = NumberConverter.deserializeDouble(jsr);
			Assert.assertEquals(d, valueParsed2, 0);
		}
	}

	@Test
	public void doubleIntRandom() throws IOException {
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final Random rnd = new Random(0);

		for (int i = 0; i < 1000000; i++) {
			sw.reset();

			// serialization
			double d = rnd.nextDouble() * rnd.nextInt();
			NumberConverter.serialize(d, sw);

			jr.process(null, sw.size());
			jr.read();

			final double valueParsed1 = NumberConverter.deserializeDouble(jr);
			Assert.assertEquals(d, valueParsed1, 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
			jsr.read();

			final double valueParsed2 = NumberConverter.deserializeDouble(jsr);
			Assert.assertEquals(d, valueParsed2, 0);
		}
	}

	@Test
	public void floatRandom() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final Random rnd = new Random(0);

		for (int i = 0; i < 1000000; i++) {
			sw.reset();

			// serialization
			float f = rnd.nextFloat();
			NumberConverter.serialize(f, sw);

			jr.process(null, sw.size());
			jr.read();

			final float valueParsed1 = NumberConverter.deserializeFloat(jr);
			Assert.assertEquals(f, valueParsed1, 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
			jsr.read();

			final float valueParsed2 = NumberConverter.deserializeFloat(jsr);
			Assert.assertEquals(f, valueParsed2, 0);
		}
	}

	@Test
	public void floatIntRandom() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final Random rnd = new Random(0);

		for (int i = 0; i < 1000000; i++) {
			sw.reset();

			// serialization
			float d = (float)rnd.nextDouble() * rnd.nextInt();
			NumberConverter.serialize(d, sw);

			jr.process(null, sw.size());
			jr.read();

			final float valueParsed1 = NumberConverter.deserializeFloat(jr);
			Assert.assertEquals(d, valueParsed1, 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
			jsr.read();

			final float valueParsed2 = NumberConverter.deserializeFloat(jsr);
			Assert.assertEquals(d, valueParsed2, 0);
		}
	}

	private void prepareJson(JsonReader<Object> reader, byte[] input) throws IOException {
		reader.process(input, input.length);
		reader.read();
		reader.read();
		reader.fillName();
		reader.read();
	}

	private double checkDoubleError(JsonReader<Object> reader, String error) {
		double res = 0;
		try {
			res = NumberConverter.deserializeDouble(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains(error));
		}
		return res;
	}

	private float checkFloatError(JsonReader<Object> reader, String error) {
		float res = 0;
		try {
			res = NumberConverter.deserializeFloat(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains(error));
		}
		return res;
	}

	private BigDecimal checkDecimalError(JsonReader<Object> reader, String error) {
		BigDecimal res = null;
		try {
			res = NumberConverter.deserializeDecimal(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains(error));
		}
		return res;
	}

	private int checkIntError(JsonReader<Object> reader, String error) {
		int res = 0;
		try {
			res = NumberConverter.deserializeInt(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains(error));
		}
		return res;
	}

	private long checkLongError(JsonReader<Object> reader, String error) {
		long res = 0;
		try {
			res = NumberConverter.deserializeLong(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains(error));
		}
		return res;
	}

	private Number checkNumberError(JsonReader<Object> reader, String error) {
		Number res = null;
		try {
			res = NumberConverter.deserializeNumber(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains(error));
		}
		return res;
	}

	@Test
	public void emptyParsing() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);

		byte[] empty = "{\"x\":}".getBytes("UTF-8");
		byte[] space = "{\"x\": }".getBytes("UTF-8");
		byte[] plus = "{\"x\":+}".getBytes("UTF-8");
		byte[] minus = "{\"x\":-}".getBytes("UTF-8");
		byte[] e = "{\"x\":e}".getBytes("UTF-8");
		byte[] plusSpace = "{\"x\":+ }".getBytes("UTF-8");
		byte[] minusSpace = "{\"x\":- }".getBytes("UTF-8");
		byte[] eSpace = "{\"x\":E }".getBytes("UTF-8");
		byte[] dot = "{\"x\":.}".getBytes("UTF-8");
		byte[] doubleMinus = "{\"x\":--0}".getBytes("UTF-8");
		byte[] doubleMinusSpace = "{\"x\":--0}".getBytes("UTF-8");
		byte[] doubleZero = "{\"x\":00}".getBytes("UTF-8");
		byte[] doubleNegativeZero = "{\"x\":-00}".getBytes("UTF-8");
		byte[] doubleZeroAndSpace = "{\"x\":00 }".getBytes("UTF-8");
		byte[] leadingZero = "{\"x\":01}".getBytes("UTF-8");
		byte[] leadingNegativeZero = "{\"x\":-01}".getBytes("UTF-8");
		byte[] leadingZeroAndSpace = "{\"x\":01 }".getBytes("UTF-8");
		byte[] quotedLeadingZero = "{\"x\":\"01\"}".getBytes("UTF-8");
		byte[] quotedLeadingNegativeZero = "{\"x\":\"-01\"}".getBytes("UTF-8");
		byte[] quotedLeadingZeroAndSpace = "{\"x\":\"01\" }".getBytes("UTF-8");

		byte[][] input = {
				empty, space, plus, minus, e, plusSpace, minusSpace, eSpace, dot, doubleMinus, doubleMinusSpace,
				doubleZero, doubleNegativeZero, doubleZeroAndSpace, leadingZero, leadingNegativeZero, leadingZeroAndSpace,
				quotedLeadingZero, quotedLeadingNegativeZero, quotedLeadingZeroAndSpace
		};

		for(byte[] it : input) {
			prepareJson(jr, it);
			checkDoubleError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkFloatError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkDecimalError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkIntError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkLongError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkNumberError(jr, "Error parsing number at position: 5");
		}
	}

	@Test
	public void zeroParsing() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);

		byte[] zeroWithSpace = "{\"x\":0 }".getBytes("UTF-8");
		byte[] negativeZeroWithSpace = "{\"x\":-0 }".getBytes("UTF-8");

		byte[][] input = {zeroWithSpace, negativeZeroWithSpace};

		for(byte[] it : input) {
			prepareJson(jr, it);
			Assert.assertEquals(0d, checkDoubleError(jr, null), 0);
			prepareJson(jr, it);
			Assert.assertEquals(0f, checkFloatError(jr, null), 0);
			prepareJson(jr, it);
			Assert.assertEquals(BigDecimal.ZERO, checkDecimalError(jr, null));
			prepareJson(jr, it);
			Assert.assertEquals(0, checkIntError(jr, null));
			prepareJson(jr, it);
			Assert.assertEquals(0L, checkLongError(jr, null));
			prepareJson(jr, it);
			Assert.assertEquals(0, checkNumberError(jr, null).intValue());
		}
	}

	@Test
	public void wrongSpaceParsing() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);

		byte[] doubleZero = "{\"x\":0 0}".getBytes("UTF-8");
		byte[] doubleDot1 = "{\"x\":0.0.}".getBytes("UTF-8");
		byte[] doubleDot2 = "{\"x\":0..0}".getBytes("UTF-8");
		byte[] dotNoNumber1 = "{\"x\":.0}".getBytes("UTF-8");
		byte[] dotNoNumber2 = "{\"x\":0.}".getBytes("UTF-8");

		byte[][] input = {doubleZero, doubleDot1, doubleDot2, dotNoNumber1, dotNoNumber2};

		for(byte[] it : input) {
			prepareJson(jr, it);
			checkDoubleError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkFloatError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkDecimalError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkIntError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkLongError(jr, "Error parsing number at position: 5");
		}
	}

	@Test
	public void specialFloats() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		String[] values = {
				Float.toString(Float.MAX_VALUE / 10),
				Float.toString(Float.MIN_VALUE * 10),
				Float.toString(Float.MIN_VALUE / 10),
				Float.toString(Float.MIN_VALUE),
				Float.toString(Float.MAX_VALUE),
				"1E46",
				"1e-46",
				"0.00000000000000001",
				"0.000000000000000001",
				"0.0000000000000000001",
				"0.0000000000000000000000000000001",
				"0.00000000000000000000000000000000000000000001",
				"0.000000000000000000000000000000000000000000001",
				"0.0000000000000000000000000000000000000000000001",
				"0.7706706532754006",
				"0.7706706532754006 ",
				"0.77067065327",
				"0.77067065327 ",
				"1000000000000000000000000",
				"100000000000000000000000000000000000000",
				"1000000000000000000000000000000000000000",
				"100000000000000000000000000000000000000.000000000000000000000001",
				"100000000000000000000000000000000000000.000000000000000000000001e-10"
		};

		for (String d : values) {
			float f = Float.parseFloat(d);

			byte[] input = d.getBytes("UTF-8");
			jr.process(input, input.length);
			jr.read();

			final float valueParsed1 = NumberConverter.deserializeFloat(jr);
			Assert.assertEquals(f, valueParsed1, 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(input, 0, input.length);
			jsr.process(is);
			jsr.read();

			final float valueParsed2 = NumberConverter.deserializeFloat(jsr);
			Assert.assertEquals(f, valueParsed2, 0);
		}
	}

	@Test
	public void doubleRoundingError() throws IOException {
		final DslJson<Object> dslJson = new DslJson<Object>(new DslJson.Settings<Object>().doublePrecision(JsonReader.DoublePrecision.DEFAULT));
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		double[] values = {
				-5983259.62725876d,
				3.999999999999999555d,
				65600.45509999999d,
				65600.45509999998d,
				-707224.705947716d,
				0.04040949867001864d,
				-5983259.62725876d,
				-1800849.97476139d,
				54940.89750944923d,
				54940.89750944924d,
				//54940.897509449234d, //TODO: doesn't work in default
				-740342.9473267009d,
				-74034294.73267009d,
				-7403429.473267009d,
				-7403429.4732670095d,
				0.6374174253501083d,
				0.6374174253501084d,
				-9.514467982939291E8d,
				0.9644868606768501d,
				0.96448686067685d,
				//2.716906186888657d, //TODO: doesn't work in default
				2.7169061868886573d,
				98.48415401998089d,
				98.48415401998088d,
				-9603443.683176761d,
				7.551599396638066E8d,
				8.484850737442602E8,
				-99.86965d,
				-777.77d,
				0.984841540199809d,
				0.9848415401998091d,
				1.111538368674174E9d,
				1.1115383686741738E9d,
				0.730967787376657d,
				0.7309677873766569d,
				Double.MIN_VALUE, Double.MAX_VALUE
		};

		for (double d : values) {
			sw.reset();

			//System.out.println("processing " + d);

			NumberConverter.serialize(d, sw);

			jr.process(null, sw.size());
			jr.read();

			final double valueParsed1 = NumberConverter.deserializeDouble(jr);
			Assert.assertEquals(d, valueParsed1, 0);

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
			jsr.read();

			final double valueParsed2 = NumberConverter.deserializeDouble(jsr);
			Assert.assertEquals(d, valueParsed2, 0);
		}
	}

	@Test
	public void zeroSpaceEndParsing() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);

		byte[] positive = "{\"x\":0 }".getBytes("UTF-8");
		byte[] negative = "{\"x\":-0 }".getBytes("UTF-8");

		byte[][] inputs = {positive, negative};
		for (byte[] input : inputs) {
			prepareJson(jr, input);
			Assert.assertEquals(0d, checkDoubleError(jr, null), 0);
			prepareJson(jr, input);
			Assert.assertEquals(0f, checkFloatError(jr, null), 0);
			prepareJson(jr, input);
			Assert.assertEquals(BigDecimal.ZERO, checkDecimalError(jr, null));
			prepareJson(jr, input);
			Assert.assertEquals(0, checkIntError(jr, null));
			prepareJson(jr, input);
			Assert.assertEquals(0L, checkLongError(jr, null));
			prepareJson(jr, input);
			Assert.assertEquals(0L, checkNumberError(jr, null));
		}
	}

	@Test
	public void decimalSpaceEndParsing() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);

		byte[] positive = "{\"x\":11.1 }".getBytes("UTF-8");
		prepareJson(jr, positive);
		Assert.assertEquals(11.1d, checkDoubleError(jr, null), 0);
		prepareJson(jr, positive);
		Assert.assertEquals(11.1f, checkFloatError(jr, null), 0);
		prepareJson(jr, positive);
		Assert.assertEquals(BigDecimal.valueOf(11.1), checkDecimalError(jr, null));
		prepareJson(jr, positive);
		checkIntError(jr, "Error parsing number at position: 5");
		prepareJson(jr, positive);
		checkLongError(jr, "Error parsing number at position: 5");
		prepareJson(jr, positive);
		Assert.assertEquals(BigDecimal.valueOf(11.1), checkNumberError(jr, null));

		byte[] negative = "{\"x\":-11.1 }".getBytes("UTF-8");
		prepareJson(jr, negative);
		Assert.assertEquals(-11.1d, checkDoubleError(jr, null), 0);
		prepareJson(jr, negative);
		Assert.assertEquals(-11.1f, checkFloatError(jr, null), 0);
		prepareJson(jr, negative);
		Assert.assertEquals(BigDecimal.valueOf(-11.1), checkDecimalError(jr, null));
		prepareJson(jr, negative);
		checkIntError(jr, "Error parsing number at position: 5");
		prepareJson(jr, negative);
		checkLongError(jr, "Error parsing number at position: 5");
		prepareJson(jr, negative);
		Assert.assertEquals(BigDecimal.valueOf(-11.1), checkNumberError(jr, null));
	}

	@Test
	public void exponentSpaceEndParsing() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);

		byte[] positive = "{\"x\":1e2 }".getBytes("UTF-8");

		prepareJson(jr, positive);
		Assert.assertEquals(1e2d, checkDoubleError(jr, null), 0);
		prepareJson(jr, positive);
		Assert.assertEquals(1e2f, checkFloatError(jr, null), 0);
		prepareJson(jr, positive);
		Assert.assertEquals(0, BigDecimal.valueOf(1e2).compareTo(checkDecimalError(jr, null)));
		prepareJson(jr, positive);
		Assert.assertEquals(100, checkIntError(jr, null));
		prepareJson(jr, positive);
		Assert.assertEquals(100L, checkLongError(jr, null));
		prepareJson(jr, positive);
		Assert.assertEquals(0, BigDecimal.valueOf(1e2).compareTo((BigDecimal)checkNumberError(jr, null)));

		byte[] negative = "{\"x\":-1e2 }".getBytes("UTF-8");
		prepareJson(jr, negative);
		Assert.assertEquals(-1e2d, checkDoubleError(jr, null), 0);
		prepareJson(jr, negative);
		Assert.assertEquals(-1e2f, checkFloatError(jr, null), 0);
		prepareJson(jr, negative);
		Assert.assertEquals(0, BigDecimal.valueOf(-1e2).compareTo(checkDecimalError(jr, null)));
		prepareJson(jr, negative);
		Assert.assertEquals(-100, checkIntError(jr, null));
		prepareJson(jr, negative);
		Assert.assertEquals(-100L, checkLongError(jr, null));
		prepareJson(jr, negative);
		Assert.assertEquals(0, BigDecimal.valueOf(-1e2).compareTo((BigDecimal)checkNumberError(jr, null)));
	}

	@Test
	public void exponentWithDecimalSpaceEndParsing() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);

		byte[] positive = "{\"x\":1.2e3 }".getBytes("UTF-8");

		prepareJson(jr, positive);
		Assert.assertEquals(1.2e3d, checkDoubleError(jr, null), 0);
		prepareJson(jr, positive);
		Assert.assertEquals(1.2e3f, checkFloatError(jr, null), 0);
		prepareJson(jr, positive);
		Assert.assertEquals(0, BigDecimal.valueOf(1.2e3).compareTo(checkDecimalError(jr, null)));
		prepareJson(jr, positive);
		Assert.assertEquals(1200, checkIntError(jr, null));
		prepareJson(jr, positive);
		Assert.assertEquals(1200L, checkLongError(jr, null));
		prepareJson(jr, positive);
		Assert.assertEquals(0, BigDecimal.valueOf(1.2e3).compareTo((BigDecimal)checkNumberError(jr, null)));

		byte[] negative = "{\"x\":-1.2e3 }".getBytes("UTF-8");
		prepareJson(jr, negative);
		Assert.assertEquals(-1.2e3d, checkDoubleError(jr, null), 0);
		prepareJson(jr, negative);
		Assert.assertEquals(-1.2e3f, checkFloatError(jr, null), 0);
		prepareJson(jr, negative);
		Assert.assertEquals(0, BigDecimal.valueOf(-1.2e3).compareTo(checkDecimalError(jr, null)));
		prepareJson(jr, negative);
		Assert.assertEquals(-1200, checkIntError(jr, null));
		prepareJson(jr, negative);
		Assert.assertEquals(-1200L, checkLongError(jr, null));
		prepareJson(jr, negative);
		Assert.assertEquals(0, BigDecimal.valueOf(-1.2e3).compareTo((BigDecimal)checkNumberError(jr, null)));
	}

	@Test
	public void smallNumbersArray() throws IOException {
		DslJson[] jsons = {new DslJson<Object>(), dslJson};
		for (DslJson<Object> json : jsons) {
			final JsonReader<Object> jr = json.newReader(new byte[0]);

			byte[] input = "[7.006492321624086e-46,0.7006492321624086e-45,7006492321624086e-61,700649232162408.6e-60,7.006492321624086000000e-46,0.007006492321624086e-43]".getBytes("UTF-8");

			jr.process(input, input.length);
			jr.read();
			jr.read();

			final float[] floats = NumberConverter.deserializeFloatArray(jr);
			Assert.assertEquals(6, floats.length);
			for (int i = 0; i < floats.length; i++) {
				Assert.assertEquals(7.006492321624086e-46f, floats[i], 0);
			}

			jr.process(input, input.length);
			jr.read();
			jr.read();

			final double[] doubles = NumberConverter.deserializeDoubleArray(jr);
			Assert.assertEquals(6, doubles.length);
			for (int i = 0; i < doubles.length; i++) {
				Assert.assertEquals(7.006492321624086e-46, doubles[i], 0);
			}
		}
		for (DslJson<Object> json : jsons) {
			final JsonReader<Object> jr = json.newReader(new byte[0]);

			byte[] input = "[7e-45,7e-45 , 7e-45, 7e-45 ,0.7e-44, 0.7e-44,0.7e-44 , 0.7e-44 ]".getBytes("UTF-8");

			jr.process(input, input.length);
			jr.read();
			jr.read();

			final float[] floats = NumberConverter.deserializeFloatArray(jr);
			Assert.assertEquals(8, floats.length);
			for (int i = 0; i < floats.length; i++) {
				Assert.assertEquals(7e-45f, floats[i], 0);
			}

			jr.process(input, input.length);
			jr.read();
			jr.read();

			final double[] doubles = NumberConverter.deserializeDoubleArray(jr);
			Assert.assertEquals(8, doubles.length);
			for (int i = 0; i < doubles.length; i++) {
				Assert.assertEquals(7e-45, doubles[i], 0);
			}
		}

		for (DslJson<Object> json : jsons) {
			final JsonReader<Object> jr = json.newReader(new byte[0]);

			byte[] input = "[1.4e-45,14e-46,0.14e-44,0.0140e-43]".getBytes("UTF-8");

			jr.process(input, input.length);
			jr.read();
			jr.read();

			final float[] floats = NumberConverter.deserializeFloatArray(jr);
			Assert.assertEquals(4, floats.length);
			for (int i = 0; i < floats.length; i++) {
				Assert.assertEquals(1.4e-45f, floats[i], 0);
			}
		}
	}

	@Test
	public void largeNumbersArray() throws IOException {
		DslJson[] jsons = {new DslJson<Object>(), dslJson};
		for (DslJson<Object> json : jsons) {
			final JsonReader<Object> jr = json.newReader(new byte[0]);

			byte[] input = "[3.4028235e+38,0.34028235e+39,34028235e+31,3402823.5e+32,3.4028235000000000000e+38,0.0034028235e+41]".getBytes("UTF-8");

			jr.process(input, input.length);
			jr.read();
			jr.read();

			final float[] floats = NumberConverter.deserializeFloatArray(jr);
			Assert.assertEquals(6, floats.length);
			for (int i = 0; i < floats.length; i++) {
				Assert.assertEquals(3.4028235e+38f, floats[i], 0);
			}

			jr.process(input, input.length);
			jr.read();
			jr.read();

			final double[] doubles = NumberConverter.deserializeDoubleArray(jr);
			Assert.assertEquals(6, doubles.length);
			for (int i = 0; i < doubles.length; i++) {
				Assert.assertEquals(3.4028235e+38, doubles[i], 0);
			}
		}
	}

	@Test
	public void edgeCasesDouble() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(null);
		final double[] input = { -2.3550447074000003E-4, -2.3550447074000003E-5, -2.3550447074000003E-6, -2.3550447074000003E-7, 1.00000017881393432617187499 };

		NumberConverter.serialize(input, sw);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		// init
		jr.process(null, sw.size());
		jr.read();
		jr.read();

		double[] numbers1 = NumberConverter.deserializeDoubleArray(jr);
		Assert.assertArrayEquals(input, numbers1, 0);

		jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
		// init
		jsr.read();
		jsr.read();

		double[] numbers2 = NumberConverter.deserializeDoubleArray(jsr);
		Assert.assertArrayEquals(input, numbers2, 0);
	}

	@Test
	public void edgeCasesFloat() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(null);
		final float[] numbers = {0.11596030607005024f,1.1596030607005024f,11.1596030607005024f,-1.1596030607005024f,1.1596031f,
				-1.1596031f,0.011596030607005024f,Float.NEGATIVE_INFINITY,0f,0f,1.00000017881393432617187499f,1.00000017881393432617187499f,
				1.0000001788139343f,1.00000017881393f};
		final byte[] input = ("[0.11596030607005024,1.1596030607005024,11.1596030607005024,-1.1596030607005024,1.1596031," +
				"-1.1596031,0.011596030607005024,-1234567890123456789e55,0E+75,0E-85,1.00000017881393432617187499,10.0000017881393432617187499E-1," +
				"1.0000001788139343,1.00000017881393]").getBytes("UTF-8");

		final JsonReader<Object> jr = dslJson.newReader(new byte[64]);
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		// init
		jr.process(input, input.length);
		jr.read();
		jr.read();

		float[] numbers1 = NumberConverter.deserializeFloatArray(jr);
		Assert.assertArrayEquals(numbers, numbers1, 0);

		jsr.process(new ByteArrayInputStream(input, 0, input.length));
		// init
		jsr.read();
		jsr.read();

		float[] numbers2 = NumberConverter.deserializeFloatArray(jsr);
		Assert.assertArrayEquals(numbers, numbers2, 0);
	}

	@Test
	public void doubleAndFloatRandom() throws IOException {
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final Random rnd = new Random(0);

		for (int i = 0; i < 1000000; i++) {
			sw.reset();

			BigDecimal bd =
				i%5 == 0 ? BigDecimal.valueOf(rnd.nextLong(), rnd.nextInt(500))
				: BigDecimal.valueOf(rnd.nextLong(), rnd.nextInt(1000) - 500);

			double d = bd.doubleValue();
			float f = bd.floatValue();

			NumberConverter.serialize(bd, sw);

			jr.process(null, sw.size());
			jr.read();
			final double dp1 = NumberConverter.deserializeDouble(jr);
			Assert.assertEquals(d, dp1, 0);

			final ByteArrayInputStream isd = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(isd);
			jsr.read();
			final double dp2 = NumberConverter.deserializeDouble(jsr);
			Assert.assertEquals(d, dp2, 0);

			jr.process(null, sw.size());
			jr.read();
			final float fp1 = NumberConverter.deserializeFloat(jr);
			Assert.assertEquals(f, fp1, 0);

			final ByteArrayInputStream isf = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(isf);
			jsr.read();
			final float fp2 = NumberConverter.deserializeFloat(jsr);
			Assert.assertEquals(f, fp2, 0);
		}
	}

	@Test
	public void supportInvalidNan() throws IOException {
		DslJson<Object> dslJson = new DslJson<Object>();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		List<Double> values = Arrays.asList(1.2, Double.NaN, Double.POSITIVE_INFINITY);
		dslJson.serialize(values, os);
		Assert.assertEquals("[1.2,\"NaN\",\"Infinity\"]", os.toString("UTF-8"));
		List<Double> resultQuoted = dslJson.deserializeList(Double.class, os.toByteArray(), os.size());
		Assert.assertEquals(values, resultQuoted);
		final JsonWriter.WriteObject<Double> doubleWriter = new JsonWriter.WriteObject<Double>() {
			@Override
			public void write(JsonWriter writer, Double value) {
				if (value == null) writer.writeNull();
				else writer.writeAscii(value.toString());
			}
		};
		dslJson.registerWriter(double.class, doubleWriter);
		dslJson.registerWriter(Double.class, doubleWriter);
		//double[].class must be also handled otherwise it will fall back to default processing
		final JsonReader.ReadObject<Double> doubleReader = new JsonReader.ReadObject<Double>() {
			@Override
			public Double read(JsonReader reader) throws IOException {
				if (reader.last() == 'N') {
					if (reader.read() == 'a' && reader.read() == 'N') return Double.NaN;
					else throw ParsingException.create("Expecting NaN", false);
				} else if (reader.last() == 'I') { //negative infinity is not handled
					if (reader.read() == 'n' && reader.read() == 'f' && reader.read() == 'i' && reader.read() == 'n' && reader.read() == 'i' && reader.read() == 't' && reader.read() == 'y') return Double.POSITIVE_INFINITY;
					else throw ParsingException.create("Expecting Infinity", false);
				} else {
					return NumberConverter.deserializeDouble(reader);
				}
			};
		};
		dslJson.registerReader(double.class, doubleReader);
		dslJson.registerReader(Double.class, doubleReader);
		os.reset();
		dslJson.serialize(values, os);
		Assert.assertEquals("[1.2,NaN,Infinity]", os.toString("UTF-8"));
		List<Double> resultUnquoted = dslJson.deserializeList(double.class, os.toByteArray(), os.size());
		Assert.assertEquals(values, resultUnquoted);
	}
}
