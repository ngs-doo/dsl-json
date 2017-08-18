package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

public class NumberConverterTest {

	private final DslJson<Object> dslJson = new DslJson<Object>();

	@Test
	public void rangeCheckInt() throws IOException {
		// setup
		final JsonWriter sw = new JsonWriter(40, null);
		final JsonReader<Object> jr = dslJson.newReader(sw.getByteBuffer());
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		final int from = -10000000;
		final int to = 10000000;

		for (long value = from; value <= to; value += 33) {
			sw.reset();

			// serialization
			NumberConverter.serialize(value, sw);

			jr.process(null, sw.size());
			jr.read();

			final long valueParsed1 = NumberConverter.deserializeLong(jr);
			Assert.assertEquals(value, valueParsed1);

			jsr.process(new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size()));
			jsr.read();

			final long valueParsed2 = NumberConverter.deserializeLong(jsr);
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

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
			jsr.read();

			final long valueParsed2 = NumberConverter.deserializeLong(jsr);
			Assert.assertEquals(value, valueParsed2);
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

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
			jsr.read();

			final BigDecimal valueParsed2 = NumberConverter.deserializeDecimal(jsr);
			Assert.assertEquals(bd, valueParsed2);
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

			final ByteArrayInputStream is = new ByteArrayInputStream(sw.getByteBuffer(), 0, sw.size());
			jsr.process(is);
			jsr.read();

			final double valueParsed2 = NumberConverter.deserializeDouble(jsr);
			Assert.assertEquals(d, valueParsed2, 0);
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
		// setup
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
		// setup
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
			Assert.assertTrue(ex.getMessage().startsWith(error));
		}
		return res;
	}

	private float checkFloatError(JsonReader<Object> reader, String error) {
		float res = 0;
		try {
			res = NumberConverter.deserializeFloat(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith(error));
		}
		return res;
	}

	private BigDecimal checkDecimalError(JsonReader<Object> reader, String error) {
		BigDecimal res = null;
		try {
			res = NumberConverter.deserializeDecimal(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith(error));
		}
		return res;
	}

	private int checkIntError(JsonReader<Object> reader, String error) {
		int res = 0;
		try {
			res = NumberConverter.deserializeInt(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith(error));
		}
		return res;
	}

	private long checkLongError(JsonReader<Object> reader, String error) {
		long res = 0;
		try {
			res = NumberConverter.deserializeLong(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith(error));
		}
		return res;
	}

	@Test
	public void emptyParsing() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		byte[] empty = "{\"x\":}".getBytes("UTF-8");
		byte[] space = "{\"x\": }".getBytes("UTF-8");
		byte[] plus = "{\"x\":+ }".getBytes("UTF-8");
		byte[] minus = "{\"x\":- }".getBytes("UTF-8");

		byte[][] input = {empty, space, plus, minus};

		for(byte[] it : input) {
			prepareJson(jr, it);
			checkDoubleError(jr, "Error parsing double at position: 5");
			prepareJson(jr, it);
			checkFloatError(jr, "Error parsing float number at position: 5");
			prepareJson(jr, it);
			checkDecimalError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkIntError(jr, "Error parsing number at position: 5");
			prepareJson(jr, it);
			checkLongError(jr, "Error parsing number at position: 5");
		}
	}

	@Test
	public void zeroParsing() throws IOException {
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);
		final JsonReader<Object> jsr = dslJson.newReader(new ByteArrayInputStream(new byte[0]), new byte[64]);

		byte[] doubleZero = "{\"x\":00}".getBytes("UTF-8");
		byte[] positiveZero = "{\"x\":+00}".getBytes("UTF-8");
		byte[] negativeZero = "{\"x\":-00}".getBytes("UTF-8");
		byte[] zeroWithSpace = "{\"x\":0 }".getBytes("UTF-8");
		byte[] negativeZeroWithSpace = "{\"x\":-0 }".getBytes("UTF-8");

		byte[][] input = {doubleZero, positiveZero, negativeZero, zeroWithSpace, negativeZeroWithSpace};

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
		}
	}
}
