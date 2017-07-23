package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;

public class DecimalConverterTest {
	private static final String VALUES =
			"0e-0, 0e-1, 0e-12, 0e+0, 0e+1, 0e+12, 0E-0, 0E-1, 0E-12, 0E+0, 0E+1, 0E+12, 0.0e-0, 0.0e-1, 0.0e-12," +
					"0.0e+0, 0.0e+1, 0.0e+12, 0.0E-0, 0.0E-1, 0.0E-12, 0.0E+0, 0.0E+1, 0.0E+12, 0.12e-0, 0.12e-1, 0.12e-12," +
					"0.12e+0, 0.12e+1, 0.12e+12, 0.12E-0, 0.12E-1, 0.12E-12, 0.12E+0, 0.12E+1, 0.12E+12, 1e-0, 1e-1, 1e-12," +
					"1e+0, 1e+1, 1e+12, 1E-0, 1E-1, 1E-12, 1E+0, 1E+1, 1E+12, 1.0e-0, 1.0e-1, 1.0e-12, 1.0e+0, 1.0e+1, 1.0e+12," +
					"1.0E-0, 1.0E-1, 1.0E-12, 1.0E+0, 1.0E+1, 1.0E+12, 1.12e-0, 1.12e-1, 1.12e-12, 1.12e+0, 1.12e+1, 1.12e+12," +
					"1.12E-0, 1.12E-1, 1.12E-12, 1.12E+0, 1.12E+1, 1.12E+12, 12e-0, 12e-1, 12e-12, 12e+0, 12e+1, 12e+12, 12E-0," +
					"12E-1, 12E-12, 12E+0, 12E+1, 12E+12, 12.0e-0, 12.0e-1, 12.0e-12, 12.0e+0, 12.0e+1, 12.0e+12, 12.0E-0, 12.0E-1," +
					"12.0E-12, 12.0E+0, 12.0E+1, 12.0E+12, 12.12e-0, 12.12e-1, 12.12e-12, 12.12e+0, 12.12e+1, 12.12e+12, 12.12E-0," +
					"12.12E-1, 12.12E-12, 12.12E+0, 12.12E+1, 12.12E+12, -0e-0, -0e-1, -0e-12, -0e+0, -0e+1, -0e+12, -0E-0, -0E-1," +
					"-0E-12, -0E+0, -0E+1, -0E+12, -0.0e-0, -0.0e-1, -0.0e-12, -0.0e+0, -0.0e+1, -0.0e+12, -0.0E-0, -0.0E-1, -0.0E-12," +
					"-0.0E+0, -0.0E+1, -0.0E+12, -0.12e-0, -0.12e-1, -0.12e-12, -0.12e+0, -0.12e+1, -0.12e+12, -0.12E-0, -0.12E-1," +
					"-0.12E-12, -0.12E+0, -0.12E+1, -0.12E+12, -1e-0, -1e-1, -1e-12, -1e+0, -1e+1, -1e+12, -1E-0, -1E-1, -1E-12, -1E+0," +
					"-1E+1, -1E+12, -1.0e-0, -1.0e-1, -1.0e-12, -1.0e+0, -1.0e+1, -1.0e+12, -1.0E-0, -1.0E-1, -1.0E-12, -1.0E+0, -1.0E+1," +
					"-1.0E+12, -1.12e-0, -1.12e-1, -1.12e-12, -1.12e+0, -1.12e+1, -1.12e+12, -1.12E-0, -1.12E-1, -1.12E-12, -1.12E+0," +
					"-1.12E+1, -1.12E+12, -12e-0, -12e-1, -12e-12, -12e+0, -12e+1, -12e+12, -12E-0, -12E-1, -12E-12, -12E+0, -12E+1," +
					"-12E+12, -12.0e-0, -12.0e-1, -12.0e-12, -12.0e+0, -12.0e+1, -12.0e+12, -12.0E-0, -12.0E-1, -12.0E-12, -12.0E+0," +
					"-12.0E+1, -12.0E+12, -12.12e-0, -12.12e-1, -12.12e-12, -12.12e+0, -12.12e+1, -12.12e+12, -12.12E-0, -12.12E-1," +
					"-12.12E-12, -12.12E+0, -12.12E+1, -12.12E+12 ";

	@Test
	public void testSerialization() throws IOException {
		// setup
		final String[] values = VALUES.split(", *");
		final int count = values.length;

		final byte[] buf = new byte[1024];
		final JsonWriter jw = new JsonWriter(buf, null);

		for (int i = 0; i < count - 1; i++) {
			// setup
			final BigDecimal direct = new BigDecimal(values[i]);
			jw.reset();

			// serialization
			NumberConverter.serialize(direct, jw);

			// check
			final BigDecimal current = new BigDecimal(jw.toString());
			final boolean equality = direct.compareTo(current) == 0;
			if (!equality) {
				Assert.fail("Written BigDecimal was not equal to the test value; " + direct + " != " + current);
			}
		}
	}

	@Test
	public void testDeserialization() throws IOException {
		// setup
		final String[] values = VALUES.split(", *");
		final int count = values.length;

		final byte[] buf = VALUES.getBytes(Charset.forName("ISO-8859-1"));
		final JsonReader jr = new JsonReader(buf, null);
		final JsonStreamReader jsr = new JsonStreamReader(new ByteArrayInputStream(buf), new byte[64], null);

		// first digit in values
		Assert.assertEquals('0', jr.getNextToken());
		Assert.assertEquals('0', jsr.getNextToken());

		for (int i = 0; i < count - 1; i++) {
			if (i > 0) {
				jr.getNextToken();//','
				jsr.getNextToken();//','
				jr.getNextToken();//' '
				jsr.getNextToken();//' '
			}

			// setup
			final BigDecimal direct = new BigDecimal(values[i]);

			// deserialiaztion
			final BigDecimal current1 = NumberConverter.deserializeDecimal(jr);
			final BigDecimal current2 = NumberConverter.deserializeDecimal(jsr);

			//check
			if (direct.compareTo(current1) != 0) {
				Assert.fail("Parsed BigDecimal was not equal to the test value; expected " + direct + ", but actual was " + current1 + ". Used value: " + values[i]);
			} else if (direct.compareTo(current2) != 0) {
				Assert.fail("Parsed BigDecimal was not equal to the test value; expected " + direct + ", but actual was " + current2 + ". Used value: " + values[i]);
			}
		}
	}

	@Test
	public void testPowersOf10() throws IOException {
		for (int i = -500; i < 500; i++) {
			final String sciForm = "1E" + i;
			final BigDecimal check = new BigDecimal(sciForm);

			// space to prevent end of stream gotcha
			final String plainForm = check.toPlainString();
			final byte[] body = plainForm.getBytes(Charset.forName("ISO-8859-1"));

			final JsonReader jr = new JsonReader<Object>(body, null);
			jr.getNextToken();
			final BigDecimal parsed1 = NumberConverter.deserializeDecimal(jr);

			final JsonStreamReader jsr = new JsonStreamReader<Object>(new ByteArrayInputStream(body), new byte[64], null);
			jsr.getNextToken();
			final BigDecimal parsed2 = NumberConverter.deserializeDecimal(jsr);

			if (parsed1.compareTo(check) != 0) {
				Assert.fail("Mismatch in decimals; expected " + check + ", but actual was " + parsed1);
			} else if (parsed2.compareTo(check) != 0) {
				Assert.fail("Mismatch in decimals; expected " + check + ", but actual was " + parsed2);
			}
		}
	}

	@Test
	public void longNumber() throws IOException {
		final BigDecimal check = new BigDecimal("0.123456789012345678901234567890123456789012345678901234567890123456789");

		final String plainForm = check.toPlainString();
		final byte[] body = plainForm.getBytes("UTF-8");

		DslJson<Object> json = new DslJson<Object>();

		final BigDecimal result = Common.deserialize(json, BigDecimal.class, body, body.length);
		Assert.assertEquals(check, result);
	}

	@Test
	public void longNumberWithSpace() throws IOException {
		final BigDecimal check = new BigDecimal("0.123456789012345678901234567890123456789012345678901234567890123456789");

		final String plainForm = check.toPlainString() + " ";
		final byte[] body = plainForm.getBytes("UTF-8");

		DslJson<Object> json = new DslJson<Object>();

		final BigDecimal result = Common.deserialize(json, BigDecimal.class, body, body.length);
		Assert.assertEquals(check, result);
	}

	@Test
	public void missingQuoteEnd() throws IOException {
		final BigDecimal check = new BigDecimal("0.123456789012345678901234567890123456789012345678901234567890123456789");

		final String plainForm = "\"" + check.toPlainString();
		final byte[] body = plainForm.getBytes("UTF-8");

		DslJson<Object> json = new DslJson<Object>();

		try {
			json.deserialize(BigDecimal.class, body, body.length);
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("at: 72"));
		}
		try {
			json.deserialize(BigDecimal.class, new ByteArrayInputStream(body, 0, body.length), new byte[64]);
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("at: 72"));
		}
	}

	@Test
	public void quoteOverflow() throws IOException {
		final byte[] body = "123123123123123123123123123123".getBytes("UTF-8");

		DslJson<Object> json = new DslJson<Object>();

		try {
			json.deserialize(Integer.class, body, body.length);
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("Integer overflow"));
		}
		try {
			json.deserialize(Integer.class, new ByteArrayInputStream(body, 0, body.length), new byte[64]);
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage().contains("Integer overflow"));
		}
	}
}
