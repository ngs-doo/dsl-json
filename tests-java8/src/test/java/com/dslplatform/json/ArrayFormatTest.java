package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ArrayFormatTest {

	@CompiledJson(formats = CompiledJson.Format.ARRAY)
	public static class MutableCtor {
		@JsonAttribute(index = 1)
		public int[] x;
		@JsonAttribute(index = 2)
		public List<String> s;
		@JsonAttribute(index = 3)
		public Double d;
	}

	@CompiledJson(formats = CompiledJson.Format.ARRAY)
	public static class ImmutableCtor {
		@JsonAttribute(index = 1)
		public final int[] x;
		@JsonAttribute(index = 2)
		public final List<String> s;
		@JsonAttribute(index = 3)
		public final Double d;

		public ImmutableCtor(int[] x, List<String> s, Double d) {
			this.x = x;
			this.s = s;
			this.d = d;
		}
	}

	public static class MutableFactory {
		@JsonAttribute(index = 1)
		public int[] x;
		@JsonAttribute(index = 2)
		public List<String> s;
		@JsonAttribute(index = 3)
		public Double d;
		private MutableFactory() {}
		@CompiledJson(formats = CompiledJson.Format.ARRAY)
		public static MutableFactory factory() {
			return new MutableFactory();
		}
	}

	public static class ImmutableFactory {
		@JsonAttribute(index = 1)
		public final int[] x;
		@JsonAttribute(index = 2)
		public final List<String> s;
		@JsonAttribute(index = 3)
		public final Double d;

		private ImmutableFactory(int[] x, List<String> s, Double d) {
			this.x = x;
			this.s = s;
			this.d = d;
		}
		@CompiledJson(formats = CompiledJson.Format.ARRAY)
		public static ImmutableFactory create(int[] x, List<String> s, Double d) {
			return new ImmutableFactory(x, s, d);
		}
	}

	private final DslJson<Object> dslJsonArray = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private final DslJson<Object> dslJsonMinimal = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).skipDefaultValues(true).includeServiceLoader());

	private final DslJson<Object>[] dslJsons = new DslJson[]{dslJsonArray, dslJsonMinimal};

	@Test
	public void mutableCtorRoundtrip() throws IOException {
		for (DslJson<Object> dslJson : dslJsons) {
			MutableCtor c = new MutableCtor();
			c.d = Double.parseDouble("123.456");
			c.s = Arrays.asList("abc", "def", null, "ghi");
			c.x = new int[]{1, -1, -0};
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dslJson.serialize(c, os);
			Assert.assertEquals("[[1,-1,0],[\"abc\",\"def\",null,\"ghi\"],123.456]", os.toString());
			MutableCtor res = dslJson.deserialize(MutableCtor.class, os.toByteArray(), os.size());
			Assert.assertEquals(c.d, res.d);
			Assert.assertEquals(c.s, res.s);
			Assert.assertArrayEquals(c.x, res.x);
		}
	}

	@Test
	public void immutableCtorRoundtrip() throws IOException {
		for (DslJson<Object> dslJson : dslJsons) {
			ImmutableCtor c = new ImmutableCtor(
					new int[]{1, -1, -0},
					Arrays.asList("abc", "def", null, "ghi"),
					Double.parseDouble("123.456")
			);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dslJson.serialize(c, os);
			Assert.assertEquals("[[1,-1,0],[\"abc\",\"def\",null,\"ghi\"],123.456]", os.toString());
			ImmutableCtor res = dslJson.deserialize(ImmutableCtor.class, os.toByteArray(), os.size());
			Assert.assertEquals(c.d, res.d);
			Assert.assertEquals(c.s, res.s);
			Assert.assertArrayEquals(c.x, res.x);
		}
	}

	@Test
	public void mutableFactoryRoundtrip() throws IOException {
		for (DslJson<Object> dslJson : dslJsons) {
			MutableFactory c = MutableFactory.factory();
			c.d = Double.parseDouble("123.456");
			c.s = Arrays.asList("abc", "def", null, "ghi");
			c.x = new int[]{1, -1, -0};
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dslJson.serialize(c, os);
			Assert.assertEquals("[[1,-1,0],[\"abc\",\"def\",null,\"ghi\"],123.456]", os.toString());
			MutableFactory res = dslJson.deserialize(MutableFactory.class, os.toByteArray(), os.size());
			Assert.assertEquals(c.d, res.d);
			Assert.assertEquals(c.s, res.s);
			Assert.assertArrayEquals(c.x, res.x);
		}
	}

	@Test
	public void immutableFactoryRoundtrip() throws IOException {
		for (DslJson<Object> dslJson : dslJsons) {
			ImmutableFactory c = ImmutableFactory.create(
					new int[]{1, -1, -0},
					Arrays.asList("abc", "def", null, "ghi"),
					Double.parseDouble("123.456")
			);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dslJson.serialize(c, os);
			Assert.assertEquals("[[1,-1,0],[\"abc\",\"def\",null,\"ghi\"],123.456]", os.toString());
			ImmutableFactory res = dslJson.deserialize(ImmutableFactory.class, os.toByteArray(), os.size());
			Assert.assertEquals(c.d, res.d);
			Assert.assertEquals(c.s, res.s);
			Assert.assertArrayEquals(c.x, res.x);
		}
	}

	public static class ImmutablePersonCtor {
		public final String firstName;
		public final String lastName;
		public final int age;

		@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
		public ImmutablePersonCtor(String firstName, String lastName, int age) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.age = age;
		}
	}

	public static class ImmutablePersonFactory {
		public final String firstName;
		public final String lastName;
		public final int age;

		private ImmutablePersonFactory(String firstName, String lastName, int age) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.age = age;
		}
		@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
		public static ImmutablePersonFactory create(String firstName, String lastName, int age) {
			return new ImmutablePersonFactory(firstName, lastName, age);
		}
	}

	@Test
	public void implicitOrderThroughCtor() throws IOException {
		ImmutablePersonCtor c = new ImmutablePersonCtor("first", "last", 42);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[\"first\",\"last\",42]", os.toString());
		ImmutablePersonCtor res = dslJsonArray.deserialize(ImmutablePersonCtor.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.firstName, res.firstName);
		Assert.assertEquals(c.lastName, res.lastName);
		Assert.assertEquals(c.age, res.age);
	}

	@Test
	public void implicitOrderThroughFactory() throws IOException {
		ImmutablePersonFactory c = ImmutablePersonFactory.create("first", "last", 42);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[\"first\",\"last\",42]", os.toString());
		ImmutablePersonFactory res = dslJsonArray.deserialize(ImmutablePersonFactory.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.firstName, res.firstName);
		Assert.assertEquals(c.lastName, res.lastName);
		Assert.assertEquals(c.age, res.age);
	}

	public static class NestedPersonFactory {
		public final String firstName;
		public final String lastName;
		public final int age;

		private NestedPersonFactory(String firstName, String lastName, int age) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.age = age;
		}
		public static class Factory {
			@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
			public static NestedPersonFactory create(String firstName, String lastName, int age) {
				return new NestedPersonFactory(firstName, lastName, age);
			}
		}
	}

	public static class CompanionPersonFactory {
		public final String firstName;
		public final String lastName;
		public final int age;

		private CompanionPersonFactory(String firstName, String lastName, int age) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.age = age;
		}
		public static final Companion Companion = new Companion();

		public static class Companion {
			@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
			public CompanionPersonFactory create(String firstName, String lastName, int age) {
				return new CompanionPersonFactory(firstName, lastName, age);
			}
		}
	}

	@Test
	public void factoryInNestedClass() throws IOException {
		NestedPersonFactory c = NestedPersonFactory.Factory.create("first", "last", 42);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[\"first\",\"last\",42]", os.toString());
		NestedPersonFactory res = dslJsonArray.deserialize(NestedPersonFactory.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.firstName, res.firstName);
		Assert.assertEquals(c.lastName, res.lastName);
		Assert.assertEquals(c.age, res.age);
	}

	@Test
	public void factoryInCompanionClass() throws IOException {
		CompanionPersonFactory c = CompanionPersonFactory.Companion.create("first", "last", 42);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[\"first\",\"last\",42]", os.toString());
		CompanionPersonFactory res = dslJsonArray.deserialize(CompanionPersonFactory.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.firstName, res.firstName);
		Assert.assertEquals(c.lastName, res.lastName);
		Assert.assertEquals(c.age, res.age);
	}
}
