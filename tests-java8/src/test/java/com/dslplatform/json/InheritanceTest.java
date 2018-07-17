package com.dslplatform.json;

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
}
