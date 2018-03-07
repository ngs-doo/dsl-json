package com.dslplatform.json;

import com.dslplatform.json.runtime.BeanDescription;
import com.dslplatform.json.runtime.DecodePropertyInfo;
import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class BindingChecksTest {

	static class MyClass {
		public int x;
		public String s;
		public long y;
	}

	private final DslJson dslJson = new DslJson();
	private final BeanDescription<MyClass> description = new BeanDescription<>(
			MyClass.class,
			MyClass::new,
			new JsonWriter.WriteObject[0],
			new DecodePropertyInfo[] {
					Settings.<MyClass, Integer>createDecoder((c, v) -> c.x = v, "x", dslJson, false, true, int.class),
					Settings.<MyClass, String>createDecoder((c, v) -> c.s = v, "s", dslJson, false, false, String.class),
					Settings.<MyClass, Long>createDecoder((c, v) -> c.y = v, "y", dslJson, false, true, long.class)
			},
			false
	);

	@Test
	public void expectingMandatoryError() {
		dslJson.registerBinder(MyClass.class, description);
		dslJson.registerReader(MyClass.class, description);
		try {
			dslJson.deserialize(MyClass.class, "{}".getBytes(), 2);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Mandatory properties (x, y) not found at position 2"));
		}
	}

	@Test
	public void singlePropertyMissing() {
		dslJson.registerBinder(MyClass.class, description);
		dslJson.registerReader(MyClass.class, description);
		try {
			byte[] bytes = "{\"x\":4}".getBytes("UTF-8");
			dslJson.deserialize(MyClass.class, bytes, bytes.length);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Mandatory property (y) not found at position 7"));
		}
	}

	@Test
	public void failOnUnknown() {
		dslJson.registerBinder(MyClass.class, description);
		dslJson.registerReader(MyClass.class, description);
		try {
			byte[] bytes = "{\"abc\":4}".getBytes("UTF-8");
			dslJson.deserialize(MyClass.class, bytes, bytes.length);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Unknown property detected: abc at position 1"));
		}
	}
}
