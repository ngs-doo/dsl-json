package com.dslplatform.json;

import com.dslplatform.json.runtime.ObjectFormatDescription;
import com.dslplatform.json.runtime.DecodePropertyInfo;
import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class BindingChecksTest {

	static class MyClass {
		public int x;
		public String s;
		public long y;
	}

	private final DslJson dslJson = new DslJson();

	public BindingChecksTest() {
		ObjectFormatDescription<MyClass, MyClass> description = ObjectFormatDescription.create(
				MyClass.class,
				MyClass::new,
				new JsonWriter.WriteObject[0],
				new DecodePropertyInfo[] {
						Settings.<MyClass, Integer>createDecoder((c, v) -> c.x = v, "x", dslJson, false, true, 0, false, int.class),
						Settings.<MyClass, String>createDecoder((c, v) -> c.s = v, "s", dslJson, false, false, 1, false, StringConverter.READER),
						Settings.<MyClass, Long>createDecoder((c, v) -> c.y = v, "y", dslJson, false, true, 2, false, long.class)
				},
				dslJson,
				false
		);
		dslJson.registerBinder(MyClass.class, description);
		dslJson.registerReader(MyClass.class, description);
	}

	@Test
	public void expectingMandatory() {
		try {
			dslJson.deserialize(MyClass.class, "{}".getBytes(), 2);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Mandatory properties (x, y) not found at position: 2"));
		}
	}

	@Test
	public void singlePropertyMissing() {
		try {
			byte[] bytes = "{\"x\":4}".getBytes("UTF-8");
			dslJson.deserialize(MyClass.class, bytes, bytes.length);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Mandatory property (y) not found at position: 7"));
		}
	}

	@Test
	public void failOnUnknown() {
		try {
			byte[] bytes = "{\"abc\":4}".getBytes("UTF-8");
			dslJson.deserialize(MyClass.class, bytes, bytes.length);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Unknown property detected: 'abc' while reading com.dslplatform.json.BindingChecksTest$MyClass at position: 1"));
		}
	}
}
