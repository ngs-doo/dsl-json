package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NumbersTest {

	@Test
	public void writeDoubleMatchesJdk() {
		checkWriteDoubles(randomDoubles(42, 150_000));
		checkWriteDoubles(edgeDoubles());
	}

	@Test
	public void writeFloatMatchesJdk() {
		checkWriteFloats(randomFloats(43, 150_000));
		checkWriteFloats(edgeFloats());
	}

	private static void checkWriteDoubles(final List<Double> values) {
		final byte[] buf = new byte[64];
		for (final double v : values) {
			final int len = RyuDouble.writeDouble(v, buf, 0);
			checkWrite("double", v, new String(buf, 0, len, StandardCharsets.US_ASCII));
		}
	}

	private static void checkWriteFloats(final List<Float> values) {
		final byte[] buf = new byte[64];
		for (final float v : values) {
			final int len = RyuFloat.writeFloat(v, buf, 0);
			checkWrite("float", v, new String(buf, 0, len, StandardCharsets.US_ASCII));
		}
	}

	private static void checkWrite(final String what, final double v, final String actual) {
		if (v == 0.0) {
			Assert.assertEquals(what + " zero", Double.toString(v), actual);
			return;
		}
		Assert.assertEquals(what + " round trip of " + v, Double.doubleToLongBits(v),
				Double.doubleToLongBits(Double.parseDouble(actual)));
		final String jdk = Double.toString(v);
		if (!jdk.equals(actual)) checkShorterOrEqual(what, v, actual, jdk);
	}

	private static void checkWrite(final String what, final float v, final String actual) {
		if (v == 0f) {
			Assert.assertEquals(what + " zero", Float.toString(v), actual);
			return;
		}
		Assert.assertEquals(what + " round trip of " + v, Float.floatToIntBits(v),
				Float.floatToIntBits(Float.parseFloat(actual)));
		final String jdk = Float.toString(v);
		if (!jdk.equals(actual)) checkShorterOrEqual(what, v, actual, jdk);
	}

	private static void checkShorterOrEqual(final String what, final double v, final String actual, final String jdk) {
		Assert.assertTrue(what + " longer than JDK for " + v + ": " + actual + " vs " + jdk,
				sigDigits(actual) <= sigDigits(jdk));
		Assert.assertEquals(what + " notation mismatch for " + v + ": " + actual + " vs " + jdk,
				jdk.indexOf('E') >= 0, actual.indexOf('E') >= 0);
	}

	private static int sigDigits(final String s) {
		final int e = s.indexOf('E');
		int n = 0;
		boolean started = false;
		for (int i = s.charAt(0) == '-' ? 1 : 0; i < e; i++) {
			final char c = s.charAt(i);
			if (c >= '0' && c <= '9') {
				if (c != '0') started = true;
				if (started) n++;
			}
		}
		return n;
	}

	private static List<Double> randomDoubles(final long seed, final int n) {
		final Random r = new Random(seed);
		final List<Double> out = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			for (;;) {
				final long bits = r.nextLong();
				if ((bits & 0x7FF0000000000000L) != 0x7FF0000000000000L) {
					out.add(Double.longBitsToDouble(bits));
					break;
				}
			}
		}
		return out;
	}

	private static List<Float> randomFloats(final long seed, final int n) {
		final Random r = new Random(seed);
		final List<Float> out = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			for (;;) {
				final int bits = r.nextInt();
				if ((bits & 0x7F800000) != 0x7F800000) {
					out.add(Float.intBitsToFloat(bits));
					break;
				}
			}
		}
		return out;
	}

	private static List<Double> edgeDoubles() {
		final List<Double> l = new ArrayList<>();
		l.add(0.0);
		l.add(-0.0);
		l.add(Double.MIN_VALUE);
		l.add(-Double.MIN_VALUE);
		l.add(Double.longBitsToDouble(3L));
		l.add(Double.MAX_VALUE);
		l.add(-Double.MAX_VALUE);
		l.add(Double.MIN_NORMAL);
		for (int e = -1074; e <= 1023; e += 97) {
			l.add(Math.scalb(1.0, e));
			l.add(-Math.scalb(1.0, e));
		}
		for (final double m : new double[]{0.1, 0.2, 0.3, 0.5, 1.5, 9.99, 1234.5678, 987654321.098765432}) {
			for (int n = 1; n <= 30; n++) {
				final double v = m / Math.pow(10, n);
				l.add(v);
				l.add(-v);
			}
		}
		l.add(1e-3);
		l.add(9.99e-4);
		l.add(1e7);
		l.add(9999999.5);
		l.add(1e16);
		l.add(1e-16);
		return l;
	}

	private static List<Float> edgeFloats() {
		final List<Float> l = new ArrayList<>();
		l.add(0f);
		l.add(-0f);
		l.add(Float.MIN_VALUE);
		l.add(-Float.MIN_VALUE);
		l.add(Float.intBitsToFloat(3));
		l.add(Float.MAX_VALUE);
		l.add(-Float.MAX_VALUE);
		l.add(Float.MIN_NORMAL);
		for (int e = -149; e <= 127; e += 7) {
			l.add(Math.scalb(1f, e));
			l.add(-Math.scalb(1f, e));
		}
		for (final float m : new float[]{0.1f, 0.2f, 0.5f, 1.5f, 9.99f, 1234.5678f}) {
			for (int n = 1; n <= 25; n++) {
				final float v = (float) (m / Math.pow(10, n));
				l.add(v);
				l.add(-v);
			}
		}
		l.add(1e-3f);
		l.add(9.99e-4f);
		l.add(1e7f);
		l.add(9999999.5f);
		return l;
	}

	@Test
	public void parseDoubleMatchesJdk() throws ParsingException {
		checkParseDoubles(randomDecimals(44, 150_000));
		for (final String s : edgeNumberStrings()) checkParseDouble(s);
	}

	@Test
	public void parseFloatMatchesJdk() throws ParsingException {
		checkParseFloats(randomDecimals(45, 150_000));
		for (final String s : edgeNumberStrings()) checkParseFloat(s);
	}

	private static void checkParseDoubles(final List<String> literals) throws ParsingException {
		for (final String s : literals) checkParseDouble(s);
	}

	private static void checkParseFloats(final List<String> literals) throws ParsingException {
		for (final String s : literals) checkParseFloat(s);
	}

	private static void checkParseDouble(final String s) throws ParsingException {
		final byte[] b = new byte[160];
		final int len = copyAscii(s, b);
		final double jdk = Double.parseDouble(s);
		final double ours = RyuParser.parseDouble(null, b, 0, len);
		Assert.assertEquals("parseDouble of " + s, Double.doubleToLongBits(jdk), Double.doubleToLongBits(ours));
	}

	private static void checkParseFloat(final String s) throws ParsingException {
		final byte[] b = new byte[160];
		final int len = copyAscii(s, b);
		final float jdk = Float.parseFloat(s);
		final float ours = RyuParser.parseFloat(null, b, 0, len);
		Assert.assertEquals("parseFloat of " + s, Float.floatToIntBits(jdk), Float.floatToIntBits(ours));
	}

	private static int copyAscii(final String s, final byte[] b) {
		final int n = s.length();
		for (int i = 0; i < n; i++) b[i] = (byte) s.charAt(i);
		return n;
	}

	private static List<String> randomDecimals(final long seed, final int n) {
		final Random r = new Random(seed);
		final StringBuilder sb = new StringBuilder(96);
		final List<String> out = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			sb.setLength(0);
			if (r.nextBoolean()) sb.append('-');
			final int ipDigits = r.nextInt(18);
			if (ipDigits == 0 || r.nextInt(4) == 0) {
				sb.append('0');
			} else {
				sb.append((char) ('1' + r.nextInt(9)));
				for (int d = 1; d < ipDigits; d++) sb.append((char) ('0' + r.nextInt(10)));
			}
			if (r.nextBoolean()) {
				sb.append('.');
				final int fd = 1 + r.nextInt(r.nextInt(3) == 0 ? 60 : 20);
				for (int d = 0; d < fd; d++) sb.append((char) ('0' + r.nextInt(10)));
			}
			if (r.nextInt(3) != 0) {
				sb.append(r.nextBoolean() ? 'e' : 'E');
				if (r.nextBoolean()) sb.append('-');
				final int ed = 1 + r.nextInt(4);
				for (int d = 0; d < ed; d++) sb.append((char) ('0' + r.nextInt(10)));
			}
			out.add(sb.toString());
		}
		return out;
	}

	private static String[] edgeNumberStrings() {
		return new String[]{
				"0", "-0", "0.0", "-0.0",
				"4.9E-324", "-4.9E-324", "5E-324",
				"1.7976931348623157E308", "-1.7976931348623157E308",
				"1.7976931348623158E308", // rounds up to +Infinity
				"2.2250738585072014E-308",
				"1e400", "-1e400", "1e-400", "-1e-400",
				"0.00099999999999999999999999999999999999999999999999",
				"123456789012345678901234567890.1234567890123456789",
				"9223372036854775807", "9223372036854775808", // long overflow boundary
				"18446744073709551615", "18446744073709551616", // unsigned long boundary
				"0.1", "0.30000000000000004", "123456789.123456789",
				"1e38", "1e-38", "1e39", "-1e39", // float overflow/underflow boundaries
				"1E+308", "1e-308", "1.5e-5",
		};
	}

	@Test
	public void invalidNumbersAreRejected() throws Exception {
		final String[][] cases = {
			{"--0",   "Unknown digit: '-'"},
			{"1.",    "Number ends with a dot"},
			{".5",    "Digit not found"},
			{"01",    "Leading zero is not allowed"},
			{"+1",    "Unknown digit: '+'"},
			{"1e",    "Digit not found"},
			{"1.2.3", "Unknown digit: '.'"},
			{"-",     "Digit not found"},
			{"1e+",   "Digit not found"},
			{"0x1",   "Unknown digit: 'x'"},
		};
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String[] c : cases) {
			final String literal = c[0];
			final String expected = c[1] + ". Error parsing number at position: 5";
			final byte[] json = ("{\"x\":" + literal + "}").getBytes(StandardCharsets.US_ASCII);
			final JsonReader<Object> reader = dsl.newReader(json);
			prepareJson(reader, json);
			try {
				NumberConverter.deserializeDouble(reader);
				Assert.fail("expected ParsingException for " + literal);
			} catch (final ParsingException ex) {
				Assert.assertTrue("wrong error for '" + literal + "' : " + ex.getMessage(),
						ex.getMessage().contains(expected));
			}
		}
	}

	private static void prepareJson(final JsonReader<Object> reader, final byte[] input) throws IOException {
		reader.process(input, input.length);
		reader.read();
		reader.read();
		reader.fillName();
		reader.read();
	}

	@Test
	public void quotedNumbersUseRyuParsing() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String literal : edgeNumberStrings()) {
			Assert.assertEquals("quoted double of " + literal, Double.doubleToLongBits(Double.parseDouble(literal)),
					Double.doubleToLongBits(deserializeQuoted(dsl, literal, true).doubleValue()));
			Assert.assertEquals("quoted float of " + literal, Float.floatToIntBits(Float.parseFloat(literal)),
					Float.floatToIntBits(deserializeQuoted(dsl, literal, false).floatValue()));
		}
	}

	@Test
	public void quotedAndUnquotedNumbersAgree() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String literal : edgeNumberStrings()) {
			Assert.assertEquals("double of " + literal, Double.doubleToLongBits(deserializeUnquoted(dsl, literal, true).doubleValue()),
					Double.doubleToLongBits(deserializeQuoted(dsl, literal, true).doubleValue()));
			Assert.assertEquals("float of " + literal, Float.floatToIntBits(deserializeUnquoted(dsl, literal, false).floatValue()),
					Float.floatToIntBits(deserializeQuoted(dsl, literal, false).floatValue()));
		}
	}

	@Test
	public void quotedSpecialValuesAreStillSupported() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		Assert.assertEquals(Double.doubleToLongBits(Double.NaN), Double.doubleToLongBits(deserializeQuoted(dsl, "NaN", true).doubleValue()));
		Assert.assertEquals(Double.doubleToLongBits(Double.POSITIVE_INFINITY), Double.doubleToLongBits(deserializeQuoted(dsl, "Infinity", true).doubleValue()));
		Assert.assertEquals(Double.doubleToLongBits(Double.NEGATIVE_INFINITY), Double.doubleToLongBits(deserializeQuoted(dsl, "-Infinity", true).doubleValue()));
		Assert.assertEquals(Float.floatToIntBits(Float.NaN), Float.floatToIntBits(deserializeQuoted(dsl, "NaN", false).floatValue()));
		Assert.assertEquals(Float.floatToIntBits(Float.POSITIVE_INFINITY), Float.floatToIntBits(deserializeQuoted(dsl, "Infinity", false).floatValue()));
		Assert.assertEquals(Float.floatToIntBits(Float.NEGATIVE_INFINITY), Float.floatToIntBits(deserializeQuoted(dsl, "-Infinity", false).floatValue()));
	}

	@Test
	public void quotedInvalidNumbersPointAtOpeningQuote() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String literal : new String[]{"01", "-01", "01 ", "", " ", "+1", "1.", "1e", "0x1"}) {
			checkQuotedError(dsl, literal, true);
			checkQuotedError(dsl, literal, false);
		}
	}

	private static void checkQuotedError(final DslJson<Object> dsl, final String literal, final boolean isDouble) throws IOException {
		final byte[] json = ("{\"x\":\"" + literal + "\"}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		try {
			if (isDouble) NumberConverter.deserializeDouble(reader);
			else NumberConverter.deserializeFloat(reader);
			Assert.fail("expected ParsingException for quoted " + literal);
		} catch (final ParsingException ex) {
			Assert.assertTrue("wrong error for quoted '" + literal + "': " + ex.getMessage(),
					ex.getMessage().contains("Error parsing number at position: 5"));
		}
	}

	private static Number deserializeQuoted(final DslJson<Object> dsl, final String literal, final boolean isDouble) throws IOException {
		final byte[] json = ("{\"x\":\"" + literal + "\"}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return isDouble ? NumberConverter.deserializeDouble(reader) : NumberConverter.deserializeFloat(reader);
	}

	private static Number deserializeUnquoted(final DslJson<Object> dsl, final String literal, final boolean isDouble) throws IOException {
		final byte[] json = ("{\"x\":" + literal + "}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return isDouble ? NumberConverter.deserializeDouble(reader) : NumberConverter.deserializeFloat(reader);
	}

	@Test
	public void quotedIntsMatchUnquoted() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final int v : new int[]{Integer.MIN_VALUE, -2147483647, -1000, -1, 0, 1, 1000, 2147483646, Integer.MAX_VALUE}) {
			final String literal = Integer.toString(v);
			Assert.assertEquals("quoted int of " + literal, v, (int) deserializeQuotedInt(dsl, literal));
			Assert.assertEquals("unquoted int of " + literal, v, (int) deserializeUnquotedInt(dsl, literal));
		}
		// non-simple content still goes through the generic path
		Assert.assertEquals(1, (int) deserializeQuotedInt(dsl, "+1"));
		Assert.assertEquals(100000, (int) deserializeQuotedInt(dsl, "1e5"));
		Assert.assertEquals(5, (int) deserializeQuotedInt(dsl, "5 "));
	}

	@Test
	public void quotedInvalidIntsPointAtOpeningQuote() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String literal : new String[]{"01", "-01", "", " ", "-", "1e", "0x1", "2147483648", "-2147483649"}) {
			final byte[] json = ("{\"x\":\"" + literal + "\"}").getBytes(StandardCharsets.US_ASCII);
			final JsonReader<Object> reader = dsl.newReader(json);
			prepareJson(reader, json);
			try {
				NumberConverter.deserializeInt(reader);
				Assert.fail("expected ParsingException for quoted " + literal);
			} catch (final ParsingException ex) {
				Assert.assertTrue("wrong error for quoted '" + literal + "': " + ex.getMessage(),
						ex.getMessage().contains("Error parsing number at position: 5"));
			}
		}
	}

	private static Number deserializeQuotedInt(final DslJson<Object> dsl, final String literal) throws IOException {
		final byte[] json = ("{\"x\":\"" + literal + "\"}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return NumberConverter.deserializeInt(reader);
	}

	private static Number deserializeUnquotedInt(final DslJson<Object> dsl, final String literal) throws IOException {
		final byte[] json = ("{\"x\":" + literal + "}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return NumberConverter.deserializeInt(reader);
	}

	@Test
	public void quotedLongsMatchUnquoted() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final long v : new long[]{Long.MIN_VALUE, Long.MIN_VALUE + 1, -1000, -1, 0, 1, 1000, Long.MAX_VALUE - 1, Long.MAX_VALUE}) {
			final String literal = Long.toString(v);
			Assert.assertEquals("quoted long of " + literal, v, deserializeQuotedLong(dsl, literal).longValue());
			Assert.assertEquals("unquoted long of " + literal, v, deserializeUnquotedLong(dsl, literal).longValue());
		}
		// non-simple content still goes through the generic path
		Assert.assertEquals(1, deserializeQuotedLong(dsl, "+1").longValue());
		Assert.assertEquals(100000, deserializeQuotedLong(dsl, "1e5").longValue());
		Assert.assertEquals(5, deserializeQuotedLong(dsl, "5 ").longValue());
	}

	@Test
	public void quotedShortsMatchUnquoted() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final int v : new int[]{Short.MIN_VALUE, Short.MIN_VALUE + 1, -1000, -1, 0, 1, 1000, Short.MAX_VALUE - 1, Short.MAX_VALUE}) {
			final String literal = Integer.toString(v);
			Assert.assertEquals("quoted short of " + literal, v, deserializeQuotedShort(dsl, literal).intValue());
			Assert.assertEquals("unquoted short of " + literal, v, deserializeUnquotedShort(dsl, literal).intValue());
		}
		Assert.assertEquals(1, deserializeQuotedShort(dsl, "+1").intValue());
	}

	@Test
	public void quotedInvalidLongsPointAtOpeningQuote() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String literal : new String[]{"01", "-01", "", " ", "-", "1e", "0x1", "1.5", "9223372036854775808", "-9223372036854775809"}) {
			checkQuotedNumberError(dsl, literal, true);
		}
	}

	@Test
	public void quotedInvalidShortsPointAtOpeningQuote() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String literal : new String[]{"01", "-01", "", " ", "-", "1e", "0x1", "32768", "-32769", "40000000000"}) {
			checkQuotedNumberError(dsl, literal, false);
		}
	}

	private static void checkQuotedNumberError(final DslJson<Object> dsl, final String literal, final boolean isLong) throws IOException {
		final byte[] json = ("{\"x\":\"" + literal + "\"}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		try {
			if (isLong) NumberConverter.deserializeLong(reader);
			else NumberConverter.deserializeShort(reader);
			Assert.fail("expected ParsingException for quoted " + literal);
		} catch (final ParsingException ex) {
			Assert.assertTrue("wrong error for quoted '" + literal + "': " + ex.getMessage(),
					ex.getMessage().contains("at position: 5"));
		}
	}

	private static Number deserializeQuotedLong(final DslJson<Object> dsl, final String literal) throws IOException {
		final byte[] json = ("{\"x\":\"" + literal + "\"}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return NumberConverter.deserializeLong(reader);
	}

	private static Number deserializeUnquotedLong(final DslJson<Object> dsl, final String literal) throws IOException {
		final byte[] json = ("{\"x\":" + literal + "}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return NumberConverter.deserializeLong(reader);
	}

	private static Number deserializeQuotedShort(final DslJson<Object> dsl, final String literal) throws IOException {
		final byte[] json = ("{\"x\":\"" + literal + "\"}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return NumberConverter.deserializeShort(reader);
	}

	private static Number deserializeUnquotedShort(final DslJson<Object> dsl, final String literal) throws IOException {
		final byte[] json = ("{\"x\":" + literal + "}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return NumberConverter.deserializeShort(reader);
	}

	@Test
	public void quotedDecimalsMatchUnquoted() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String literal : new String[]{"0", "-0", "1", "-1", "0.0", "-0.0", "0.1",
				"123456789.123456789", "1e5", "-1e-5", "1E+3", "2.5E-3", "9999999999999999999", "-9999999999999999999"}) {
			Assert.assertEquals("quoted decimal of " + literal, 0, ((BigDecimal) deserializeQuotedDecimal(dsl, literal)).compareTo(new BigDecimal(literal)));
			Assert.assertEquals("unquoted decimal of " + literal, 0, ((BigDecimal) deserializeUnquotedDecimal(dsl, literal)).compareTo(new BigDecimal(literal)));
		}
		// non-simple content still goes through the generic path
		Assert.assertEquals(0, ((BigDecimal) deserializeQuotedDecimal(dsl, "+1.5")).compareTo(new BigDecimal("1.5")));
		Assert.assertEquals(0, ((BigDecimal) deserializeQuotedDecimal(dsl, "5.")).compareTo(new BigDecimal("5")));
		Assert.assertEquals(0, ((BigDecimal) deserializeQuotedDecimal(dsl, "5 ")).compareTo(new BigDecimal("5")));
	}

	@Test
	public void quotedInvalidDecimalsPointAtOpeningQuote() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String literal : new String[]{"01", "-01", "", "-", " 1.5"}) {
			final byte[] json = ("{\"x\":\"" + literal + "\"}").getBytes(StandardCharsets.US_ASCII);
			final JsonReader<Object> reader = dsl.newReader(json);
			prepareJson(reader, json);
			try {
				NumberConverter.deserializeDecimal(reader);
				Assert.fail("expected ParsingException for quoted " + literal);
			} catch (final ParsingException ex) {
				Assert.assertTrue("wrong error for quoted '" + literal + "': " + ex.getMessage(),
						ex.getMessage().contains("at position: 5"));
			}
		}
	}

	@Test
	public void quotedDecimalSplitAcrossStreamBlocks() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		final byte[] input = "{\"x\":\"1234567890.12345678\",\"y\":9}".getBytes(StandardCharsets.US_ASCII);
		for (final int bufSize : new int[]{8, 16, 32}) {
			final JsonReader<Object> reader = dsl.newReader(new ByteArrayInputStream(input), new byte[bufSize]);
			reader.read();
			reader.read();
			reader.fillName();
			reader.read();
			Assert.assertEquals("decimal split at buffer " + bufSize, 0,
					((BigDecimal) NumberConverter.deserializeDecimal(reader)).compareTo(new BigDecimal("1234567890.12345678")));
		}
	}

	private static Number deserializeQuotedDecimal(final DslJson<Object> dsl, final String literal) throws IOException {
		final byte[] json = ("{\"x\":\"" + literal + "\"}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return NumberConverter.deserializeDecimal(reader);
	}

	private static Number deserializeUnquotedDecimal(final DslJson<Object> dsl, final String literal) throws IOException {
		final byte[] json = ("{\"x\":" + literal + "}").getBytes(StandardCharsets.US_ASCII);
		final JsonReader<Object> reader = dsl.newReader(json);
		prepareJson(reader, json);
		return NumberConverter.deserializeDecimal(reader);
	}

	@Test
	public void numberSplitAcrossStreamBlocks() throws IOException {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		final byte[] input = "{\"x\":1234567890.12345678,\"y\":9}".getBytes(StandardCharsets.US_ASCII);
		for (final int bufSize : new int[]{8, 16, 32}) {
			final JsonReader<Object> reader = dsl.newReader(new ByteArrayInputStream(input), new byte[bufSize]);
			reader.read();
			reader.read();
			reader.fillName();
			reader.read();
			Assert.assertEquals("double split at buffer " + bufSize, 1234567890.12345678d, NumberConverter.deserializeDouble(reader), 0);
		}
	}

	@Test
	public void zeroRoundTrip() throws Exception {
		final DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>());
		for (final String json : new String[]{"0", "-0"}) {
			final byte[] b = json.getBytes(StandardCharsets.UTF_8);
			final JsonReader<Object> r = dsl.newReader(b);
			r.process(null, b.length);
			r.read();
			final double v = NumberConverter.deserializeDouble(r);
			Assert.assertEquals(json, 0.0, v, 0.0);
		}
	}

	@Test
	public void fullWriteBattery() {
		checkWriteDoubles(randomDoubles(1, 5_000_000));
		checkWriteFloats(randomFloats(2, 5_000_000));
	}

	@Test
	public void fullParseBattery() throws ParsingException {
		checkParseDoubles(randomDecimals(3, 3_000_000));
		checkParseFloats(randomDecimals(4, 3_000_000));
	}

	@Test
	public void doubleLargeValuesRegression() throws IOException {
		final double[] values = {
				2E15,
				3E15,
				4E15,
				5E15,
				6E15,
				7E15,
				2670986322885633.0
		};

		final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());
		final JsonWriter writer = new JsonWriter(40, null);
		final JsonReader<Object> reader = dslJson.newReader(writer.getByteBuffer());

		for (double value : values) {
			writer.reset();
			NumberConverter.serialize(value, writer);

			reader.process(null, writer.size());
			reader.getNextToken();

			final double actual = NumberConverter.deserializeDouble(reader);
			Assert.assertEquals(value, actual, 0);
		}
	}
}
