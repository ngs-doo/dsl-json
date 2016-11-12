package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ReaderTest {

	@Test
	public void testLastName() throws IOException {
		final byte[] buf = "\"number\":1234".getBytes("UTF-8");
		final JsonReader<Object> jr = new JsonReader<Object>(buf, null);
		jr.getNextToken();
		jr.fillName();
		Assert.assertEquals("number", jr.getLastName());
		jr.getNextToken();
		Assert.assertTrue(jr.wasLastName("number"));
		int num = NumberConverter.deserializeInt(jr);
		Assert.assertEquals(1234, num);
	}
}
