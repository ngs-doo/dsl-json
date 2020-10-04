package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InheritanceTest {

	@CompiledJson
	public static abstract class Person {

		private final String name;

		protected Person(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	@CompiledJson
	public abstract static class Parent extends Person {

		public Parent(String name) {
			super(name);
		}
	}

	public static class Mother extends Parent {

		@CompiledJson
		public Mother(String name) {
			super(name);
		}
	}

	public static class Father extends Parent {

		@CompiledJson
		public Father(String name) {
			super(name);
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@CompiledJson
	public static abstract class BaseFields {

		private final String string;

		protected BaseFields(String string) {
			this.string = string;
		}

		public String getString() {
			return string;
		}
	}

	@CompiledJson(formats={CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class MainFieldsWithConstant extends BaseFields {

		private final int integer;

		public MainFieldsWithConstant(int integer) {
			super("name");
			this.integer = integer;
		}

		public int getInteger() {
			return integer;
		}
	}

	@CompiledJson
	public static class MainFieldsPassThrough extends BaseFields {

		private final int integer;

		public MainFieldsPassThrough(int integer, String string) {
			super(string);
			this.integer = integer;
		}

		public int getInteger() {
			return integer;
		}
	}

	@Test
	public void topLevel() throws IOException {
		Father f = new Father("abc");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(f, os);
		Assert.assertEquals("{\"name\":\"abc\"}", os.toString());
		Father res = dslJson.deserialize(Father.class, os.toByteArray(), os.size());
		Assert.assertEquals(f.getName(), res.getName());
	}

	@Test
	public void firstLevel() throws IOException {
		Father f = new Father("abc");
		JsonWriter jw = dslJson.newWriter();
		dslJson.serialize(jw, Parent.class, f);
		Assert.assertEquals("{\"$type\":\"com.dslplatform.json.InheritanceTest.Father\",\"name\":\"abc\"}", jw.toString());
		Parent res = dslJson.deserialize(Parent.class, jw.getByteBuffer(), jw.size());
		Assert.assertEquals(f.getName(), res.getName());
	}

	@Test
	public void secondLevel() throws IOException {
		Mother f = new Mother("abc");
		JsonWriter jw = dslJson.newWriter();
		dslJson.serialize(jw, Person.class, f);
		Assert.assertEquals("{\"$type\":\"com.dslplatform.json.InheritanceTest.Mother\",\"name\":\"abc\"}", jw.toString());
		Person res = dslJson.deserialize(Person.class, jw.getByteBuffer(), jw.size());
		Assert.assertEquals(f.getName(), res.getName());
	}

	@Test
	public void willIncludeBaseFieldsWhenConstant() throws IOException {
		MainFieldsWithConstant model = new MainFieldsWithConstant(505);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(model, os);
		Assert.assertEquals("{\"integer\":505,\"string\":\"name\"}", os.toString());
		MainFieldsWithConstant res1 = dslJson.deserialize(MainFieldsWithConstant.class, os.toByteArray(), os.size());
		Assert.assertEquals(model.getInteger(), res1.getInteger());
		Assert.assertEquals(model.getString(), res1.getString());
		byte[] noName = "{\"integer\":1}".getBytes("UTF-8");
		MainFieldsWithConstant res2 = dslJson.deserialize(MainFieldsWithConstant.class, noName, noName.length);
		Assert.assertEquals(1, res2.getInteger());
		Assert.assertEquals("name", res2.getString());
	}

	@Test
	public void willIncludeBaseFieldsWhenPassThrough() throws IOException {
		MainFieldsPassThrough model = new MainFieldsPassThrough(101, "abc");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(model, os);
		Assert.assertEquals("{\"integer\":101,\"string\":\"abc\"}", os.toString());
		MainFieldsPassThrough res1 = dslJson.deserialize(MainFieldsPassThrough.class, os.toByteArray(), os.size());
		Assert.assertEquals(model.getInteger(), res1.getInteger());
		Assert.assertEquals(model.getString(), res1.getString());
		byte[] noName = "{\"integer\":1}".getBytes("UTF-8");
		MainFieldsPassThrough res2 = dslJson.deserialize(MainFieldsPassThrough.class, noName, noName.length);
		Assert.assertEquals(1, res2.getInteger());
		Assert.assertNull(res2.getString());
	}

	@Test
	public void willIncludeBaseFieldsWhenConstantAsArray() throws IOException {
		final DslJson<Object> dslJsonArray = new DslJson<>(Settings.basicSetup().allowArrayFormat(true));
		MainFieldsWithConstant model = new MainFieldsWithConstant(505);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(model, os);
		Assert.assertEquals("[505,\"name\"]", os.toString());
		try {
			dslJson.deserialize(MainFieldsWithConstant.class, os.toByteArray(), os.size());
			Assert.fail();
		} catch (ParsingException ex) {
			Assert.assertEquals("Expecting ']' for object end. Found , at position: 5, following: `[505,`, before: `\"name\"]`", ex.getMessage());
		}
		byte[] asArray = "[505]".getBytes("UTF-8");
		MainFieldsWithConstant res1 = dslJson.deserialize(MainFieldsWithConstant.class, asArray, asArray.length);
		Assert.assertEquals(model.getInteger(), res1.getInteger());
		Assert.assertEquals(model.getString(), res1.getString());
		byte[] noName = "{\"integer\":1}".getBytes("UTF-8");
		MainFieldsWithConstant res2 = dslJson.deserialize(MainFieldsWithConstant.class, noName, noName.length);
		Assert.assertEquals(1, res2.getInteger());
		Assert.assertEquals("name", res2.getString());
	}

}
