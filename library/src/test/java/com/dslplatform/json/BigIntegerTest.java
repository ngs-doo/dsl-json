package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class BigIntegerTest {

	@Test
	public void testSmallValues() throws IOException {
		BigInteger[] values = new BigInteger[]{
				BigInteger.ZERO,
				BigInteger.valueOf(1),
				BigInteger.valueOf(-1),
				BigInteger.valueOf(Integer.MAX_VALUE),
				BigInteger.valueOf(12345678901234567L),
				BigInteger.valueOf(Long.MAX_VALUE)
		};
		JsonWriter jw = new JsonWriter(null);
		JsonReader jr = new JsonReader(new byte[0], null);
		for (BigInteger v : values) {
			jw.reset();
			BigIntegerConverter.serialize(v, jw);
			jr.process(jw.getByteBuffer(), jw.size());
			jr.read();
			BigInteger d = BigIntegerConverter.deserialize(jr);
			Assert.assertEquals(v, d);
		}
	}

	@Test
	public void testRandomValues() throws IOException {
		Random rnd = new Random();
		JsonWriter jw = new JsonWriter(null);
		JsonReader jr = new JsonReader(new byte[0], null);
		for(int i = 1;i < 500; i++) {
			BigInteger v = new BigInteger(i, rnd);
			jw.reset();
			BigIntegerConverter.serialize(v, jw);
			jr.process(jw.getByteBuffer(), jw.size());
			jr.read();
			BigInteger d = BigIntegerConverter.deserialize(jr);
			Assert.assertEquals(v, d);
		}
	}


	private void prepareJson(JsonReader<Object> reader, byte[] input) throws IOException {
		reader.process(input, input.length);
		reader.read();
		reader.read();
		reader.fillName();
		reader.read();
	}

	private BigInteger checkError(JsonReader<Object> reader, String error) {
		BigInteger res = BigInteger.ZERO;
		try {
			res = BigIntegerConverter.deserialize(reader);
			if (error != null) Assert.fail("Expecting " + error);
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith(error));
		}
		return res;
	}

	@Test
	public void zeroParsing() throws IOException {
		final DslJson<Object> dslJson = new DslJson<Object>();
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);

		byte[] doubleZero = "{\"x\":00}".getBytes("UTF-8");
		byte[] negativeZero = "{\"x\":-00}".getBytes("UTF-8");
		byte[] zeroWithSpace = "{\"x\":0 }".getBytes("UTF-8");
		byte[] negativeZeroWithSpace = "{\"x\":-0 }".getBytes("UTF-8");

		byte[][] input = {doubleZero, negativeZero, zeroWithSpace, negativeZeroWithSpace};

		for(byte[] it : input) {
			prepareJson(jr, it);
			Assert.assertEquals(BigInteger.ZERO, checkError(jr, null));
		}
	}

	@Test
	public void wrongSpaceParsing() throws IOException {
		final DslJson<Object> dslJson = new DslJson<Object>();
		final JsonReader<Object> jr = dslJson.newReader(new byte[0]);

		byte[] doubleZero = "{\"x\":0 0}".getBytes("UTF-8");
		byte[] doubleDot1 = "{\"x\":0.0.}".getBytes("UTF-8");
		byte[] doubleDot2 = "{\"x\":0..0}".getBytes("UTF-8");
		byte[] dotNoNumber1 = "{\"x\":.0}".getBytes("UTF-8");
		byte[] dotNoNumber2 = "{\"x\":0.}".getBytes("UTF-8");

		byte[][] input = {doubleZero, doubleDot1, doubleDot2, dotNoNumber1, dotNoNumber2};

		for(byte[] it : input) {
			prepareJson(jr, it);
			checkError(jr, "Error parsing number at position: 5");
		}
	}
}
