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
	public static class Composite {
		@JsonAttribute(index = 1)
		public int[] x;
		@JsonAttribute(index = 2)
		public List<String> s;
		@JsonAttribute(index = 3)
		public Double d;
	}

	@CompiledJson(formats = CompiledJson.Format.ARRAY)
	public static class ImmutableComposite {
		@JsonAttribute(index = 1)
		public final int[] x;
		@JsonAttribute(index = 2)
		public final List<String> s;
		@JsonAttribute(index = 3)
		public final Double d;

		public ImmutableComposite(int[] x, List<String> s, Double d) {
			this.x = x;
			this.s = s;
			this.d = d;
		}
	}

	private final DslJson<Object> dslJsonArray = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private final DslJson<Object> dslJsonMinimal = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).skipDefaultValues(true).includeServiceLoader());

	private final DslJson<Object>[] dslJsons = new DslJson[]{dslJsonArray, dslJsonMinimal};

	@Test
	public void objectRoundtrip() throws IOException {
		for (DslJson<Object> dslJson : dslJsons) {
			Composite c = new Composite();
			c.d = Double.parseDouble("123.456");
			c.s = Arrays.asList("abc", "def", null, "ghi");
			c.x = new int[]{1, -1, -0};
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dslJson.serialize(c, os);
			Assert.assertEquals("[[1,-1,0],[\"abc\",\"def\",null,\"ghi\"],123.456]", os.toString());
			Composite res = dslJson.deserialize(Composite.class, os.toByteArray(), os.size());
			Assert.assertEquals(c.d, res.d);
			Assert.assertEquals(c.s, res.s);
			Assert.assertArrayEquals(c.x, res.x);
		}
	}

	@Test
	public void immutableRoundtrip() throws IOException {
		for (DslJson<Object> dslJson : dslJsons) {
			ImmutableComposite c = new ImmutableComposite(
					new int[]{1, -1, -0},
					Arrays.asList("abc", "def", null, "ghi"),
					Double.parseDouble("123.456")
			);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dslJson.serialize(c, os);
			Assert.assertEquals("[[1,-1,0],[\"abc\",\"def\",null,\"ghi\"],123.456]", os.toString());
			ImmutableComposite res = dslJson.deserialize(ImmutableComposite.class, os.toByteArray(), os.size());
			Assert.assertEquals(c.d, res.d);
			Assert.assertEquals(c.s, res.s);
			Assert.assertArrayEquals(c.x, res.x);
		}
	}

	public static class ImmutablePerson {
		public final String firstName;
		public final String lastName;
		public final int age;

		@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
		public ImmutablePerson(String firstName, String lastName, int age) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.age = age;
		}
	}

	@Test
	public void implicitOrderThroughCtor() throws IOException {
		ImmutablePerson c = new ImmutablePerson("first", "last", 42);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[\"first\",\"last\",42]", os.toString());
		ImmutablePerson res = dslJsonArray.deserialize(ImmutablePerson.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.firstName, res.firstName);
		Assert.assertEquals(c.lastName, res.lastName);
		Assert.assertEquals(c.age, res.age);
	}
}
