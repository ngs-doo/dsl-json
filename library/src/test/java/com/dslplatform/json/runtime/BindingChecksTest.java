package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.StringConverter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;


public class BindingChecksTest {

	static class MyClass {
		public int x;
		public String s;
		public long y;
	}

	private final DslJson<Object> dslJson = new DslJson<Object>();

	public BindingChecksTest() {
		ObjectFormatDescription<MyClass, MyClass> description = ObjectFormatDescription.create(
				MyClass.class,
				new InstanceFactory<MyClass>() {
					@Override
					public MyClass create() {
						return new MyClass();
					}
				},
				new JsonWriter.WriteObject[0],
				new DecodePropertyInfo[] {
						Settings.createDecoder(new BiConsumer<MyClass, Integer>() {
							@Override
							public void accept(MyClass c, Integer v) {
								c.x = v;
							}
						}, "x", dslJson, false, true, 0, false, int.class),
						Settings.createDecoder(new BiConsumer<MyClass, String>() {
							@Override
							public void accept(MyClass c, String v) {
								c.s = v;
							}
						}, "s", dslJson, false, false, 1, false, StringConverter.READER),
						Settings.createDecoder(new BiConsumer<MyClass, Long>() {
							@Override
							public void accept(MyClass c, Long v) {
								c.y = v;
							}
						}, "y", dslJson, false, true, 2, false, long.class)
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
			Assert.assertTrue(ex.getMessage().contains("Unknown property detected: 'abc' while reading com.dslplatform.json.runtime.BindingChecksTest$MyClass at position: 1"));
		}
	}
}
