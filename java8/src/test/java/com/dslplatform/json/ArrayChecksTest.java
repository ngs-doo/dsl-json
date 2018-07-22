package com.dslplatform.json;

import com.dslplatform.json.runtime.ArrayFormatDescription;
import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ArrayChecksTest {

	static class MyClass {
		public int x;
		public String s;
		public long y;
	}

	private final DslJson dslJson = new DslJson();

	public ArrayChecksTest() {
		ArrayFormatDescription<MyClass, MyClass> description = ArrayFormatDescription.create(
				MyClass.class,
				MyClass::new,
				new JsonWriter.WriteObject[] {
						Settings.<MyClass, Integer>createArrayEncoder(c -> c.x, dslJson, int.class),
						Settings.<MyClass, String>createArrayEncoder(c -> c.s, dslJson, String.class),
						Settings.<MyClass, Long>createArrayEncoder(c -> c.y, NumberConverter.LONG_WRITER)
				},
				new JsonReader.BindObject[] {
						Settings.<MyClass, Integer>createArrayDecoder((c, v) -> c.x = v, NumberConverter.INT_READER),
						Settings.<MyClass, String>createArrayDecoder((c, v) -> c.s = v, dslJson, String.class),
						Settings.<MyClass, Long>createArrayDecoder((c, v) -> c.y = v, dslJson, long.class)
				}
		);
		dslJson.registerBinder(MyClass.class, description);
		dslJson.registerReader(MyClass.class, description);
	}

	@Test
	public void propertyMissing() {
		try {
			dslJson.deserialize(MyClass.class, "[]".getBytes(), 2);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Error parsing number at position: 1"));
		}
	}

	@Test
	public void extraProperties() {
		try {
			byte[] bytes = "[1,\"t\",3,4]".getBytes("UTF-8");
			dslJson.deserialize(MyClass.class, bytes, bytes.length);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Expecting ']' at position: 10, following: `[1,\"t\",3,4` while decoding com.dslplatform.json.ArrayChecksTest$MyClass. Found 4"));
		}
	}
}
