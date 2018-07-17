package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
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
				new InstanceFactory<MyClass>() {
					@Override
					public MyClass create() {
						return new MyClass();
					}
				},
				new JsonWriter.WriteObject[] {
						Settings.createArrayEncoder(new Function<MyClass, Integer>() {
							@Override
							public Integer apply(MyClass c) {
								return c.x;
							}
						}, dslJson, int.class),
						Settings.createArrayEncoder(new Function<MyClass, String>() {
							@Override
							public String apply(MyClass c) {
								return c.s;
							}
						}, dslJson, String.class),
						Settings.createArrayEncoder(new Function<MyClass, Long>() {
							@Override
							public Long apply(MyClass c) {
								return c.y;
							}
						}, NumberConverter.LONG_WRITER)
				},
				new JsonReader.BindObject[] {
						Settings.createArrayDecoder(new BiConsumer<MyClass, Integer>() {
							@Override
							public void accept(MyClass c, Integer v) {
								c.x = v;
							}
						}, NumberConverter.INT_READER),
						Settings.createArrayDecoder(new BiConsumer<MyClass, String>() {
							@Override
							public void accept(MyClass c, String v) {
								c.s = v;
							}
						}, dslJson, String.class),
						Settings.createArrayDecoder(new BiConsumer<MyClass, Long>() {
							@Override
							public void accept(MyClass c, Long v) {
								c.y = v;
							}
						}, dslJson, long.class)
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
			Assert.assertTrue(ex.getMessage().contains("Expecting ']' at position: 10 while decoding com.dslplatform.json.runtime.ArrayChecksTest$MyClass. Found 4"));
		}
	}
}
