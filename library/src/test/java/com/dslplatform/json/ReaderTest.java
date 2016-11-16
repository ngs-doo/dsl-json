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

	@Test
	public void testCalcHashNameEndSameAsFillName() throws IOException {
		final byte[] buf = "\"number\":1234".getBytes("UTF-8");
		final JsonReader<Object> jr = new JsonReader<Object>(buf, null);
		jr.getNextToken();
		jr.calcHash();
		Assert.assertTrue(jr.wasLastName("number"));
		Assert.assertEquals("number", jr.getLastName());
	}

	static class Implementation implements Interface {}
	interface Interface {}

	@Test
	public void testReaderOnInterface() throws IOException {
		DslJson<Object> dslJson = new DslJson<Object>();
		dslJson.registerReader(Implementation.class, new JsonReader.ReadObject<Implementation>() {
			@Override
			public Implementation read(JsonReader reader) throws IOException {
				return null;
			}
		});
		JsonReader.ReadObject<?> reader1 = dslJson.tryFindReader(Interface.class);
		Assert.assertNull(reader1);
		JsonReader.ReadObject<?> reader2 = dslJson.tryFindReader(Implementation.class);
		Assert.assertNotNull(reader2);
		dslJson.registerReader(Interface.class, dslJson.tryFindReader(Implementation.class));
		JsonReader.ReadObject<?> reader3 = dslJson.tryFindReader(Interface.class);
		Assert.assertNotNull(reader3);
	}
}
