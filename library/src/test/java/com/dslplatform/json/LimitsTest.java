package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

public class LimitsTest {

	@Test
	public void numberLimit() throws IOException {
		DslJson<Object> json = new DslJson<Object>(new DslJson.Settings<Object>().limitDigitsBuffer(20));
		byte[] input = "123456789012345678901234567890".getBytes("UTF-8");
		try {
			json.deserialize(BigDecimal.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertEquals("Too many digits detected in number: '30' at position: 0, before: `12345678901234567890`", ex.getMessage());
		}
	}

	@Test
	public void numberLimitMinInfo() throws IOException {
		DslJson<Object> json = new DslJson<Object>(new DslJson.Settings<Object>().limitDigitsBuffer(20).errorInfo(JsonReader.ErrorInfo.MINIMAL));
		byte[] input = "123456789012345678901234567890".getBytes("UTF-8");
		try {
			json.deserialize(BigDecimal.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertEquals("Too many digits detected in number", ex.getMessage());
		}
	}

	@Test
	public void stringLimit() throws IOException {
		DslJson<Object> json = new DslJson<Object>(new DslJson.Settings<Object>().limitStringBuffer(70));
		byte[] input = "\"012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\"".getBytes("UTF-8");
		try {
			json.deserialize(String.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertEquals("Maximum string buffer limit exceeded: '70' at position: 1, following: `\"`, before: `01234567890123456789`", ex.getMessage());
		}
	}
}
