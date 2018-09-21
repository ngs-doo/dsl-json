package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CombinedFormatTest {

	@CompiledJson(formats = {CompiledJson.Format.ARRAY,CompiledJson.Format.OBJECT})
	public static class Composite1 {
		@JsonAttribute(index = 1)
		public int[] x;
		@JsonAttribute(index = 2)
		public List<String> s;
		@JsonAttribute(index = 3, converter = DoubleConverter.class)
		public Double d;
	}
	@CompiledJson(formats = {CompiledJson.Format.OBJECT,CompiledJson.Format.ARRAY})
	public static class Composite2 {
		@JsonAttribute(index = 1)
		public int[] x;
		@JsonAttribute(index = 2)
		public List<String> s;
		@JsonAttribute(index = 3)
		public Double d;
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY,CompiledJson.Format.OBJECT})
	public static class ImmutableComposite1 {
		@JsonAttribute(index = 1)
		public final int[] x;
		@JsonAttribute(index = 2)
		public final List<String> s;
		@JsonAttribute(index = 3, converter = DoubleConverter.class)
		public final Double d;

		public ImmutableComposite1(int[] x, List<String> s, Double d) {
			this.x = x;
			this.s = s;
			this.d = d;
		}
	}
	@CompiledJson(formats = {CompiledJson.Format.OBJECT,CompiledJson.Format.ARRAY})
	public static class ImmutableComposite2 {
		@JsonAttribute(index = 1)
		public final int[] x;
		@JsonAttribute(index = 2)
		public final List<String> s;
		@JsonAttribute(index = 3)
		public final Double d;

		public ImmutableComposite2(int[] x, List<String> s, Double d) {
			this.x = x;
			this.s = s;
			this.d = d;
		}
	}

	public static abstract class DoubleConverter {
		public static final JsonReader.ReadObject<Double> JSON_READER = NumberConverter.DOUBLE_READER;
		public static JsonWriter.WriteObject<Double> JSON_WRITER() {
			return NumberConverter.DOUBLE_WRITER;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());

	@Test
	public void firstArrayThenObjectForEmptyCtor() throws IOException {
		Composite1 c = new Composite1();
		c.d = Double.parseDouble("123.456");
		c.s = Arrays.asList("abc", "def", null, "ghi");
		c.x = new int[] { 1, -1, -0 };
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("[[1,-1,0],[\"abc\",\"def\",null,\"ghi\"],123.456]", os.toString());
		Composite1 res1 = dslJson.deserialize(Composite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res1.d);
		Assert.assertEquals(c.s, res1.s);
		Assert.assertArrayEquals(c.x, res1.x);
		Composite2 res2 = dslJson.deserialize(Composite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res2.d);
		Assert.assertEquals(c.s, res2.s);
		Assert.assertArrayEquals(c.x, res2.x);
	}

	@Test
	public void firstObjectThenArrayForEmptyCtor() throws IOException {
		Composite2 c = new Composite2();
		c.d = Double.parseDouble("123.456");
		c.s = Arrays.asList("abc", "def", null, "ghi");
		c.x = new int[] { 1, -1, -0 };
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("{\"x\":[1,-1,0],\"s\":[\"abc\",\"def\",null,\"ghi\"],\"d\":123.456}", os.toString());
		Composite1 res1 = dslJson.deserialize(Composite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res1.d);
		Assert.assertEquals(c.s, res1.s);
		Assert.assertArrayEquals(c.x, res1.x);
		Composite2 res2 = dslJson.deserialize(Composite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res2.d);
		Assert.assertEquals(c.s, res2.s);
		Assert.assertArrayEquals(c.x, res2.x);
	}

	@Test
	public void firstArrayThenObjectForNonEmptyCtor() throws IOException {
		ImmutableComposite1 c = new ImmutableComposite1(
				new int[] { 1, -1, -0 },
				Arrays.asList("abc", "def", null, "ghi"),
				Double.parseDouble("123.456")
		);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("[[1,-1,0],[\"abc\",\"def\",null,\"ghi\"],123.456]", os.toString());
		ImmutableComposite1 res1 = dslJson.deserialize(ImmutableComposite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res1.d);
		Assert.assertEquals(c.s, res1.s);
		Assert.assertArrayEquals(c.x, res1.x);
		ImmutableComposite2 res2 = dslJson.deserialize(ImmutableComposite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res2.d);
		Assert.assertEquals(c.s, res2.s);
		Assert.assertArrayEquals(c.x, res2.x);
	}

	@Test
	public void firstObjectThenArrayForNonEmptyCtor() throws IOException {
		ImmutableComposite2 c = new ImmutableComposite2(
				new int[] { 1, -1, -0 },
				Arrays.asList("abc", "def", null, "ghi"),
				Double.parseDouble("123.456")
		);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("{\"x\":[1,-1,0],\"s\":[\"abc\",\"def\",null,\"ghi\"],\"d\":123.456}", os.toString());
		ImmutableComposite1 res1 = dslJson.deserialize(ImmutableComposite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res1.d);
		Assert.assertEquals(c.s, res1.s);
		Assert.assertArrayEquals(c.x, res1.x);
		ImmutableComposite2 res2 = dslJson.deserialize(ImmutableComposite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res2.d);
		Assert.assertEquals(c.s, res2.s);
		Assert.assertArrayEquals(c.x, res2.x);
	}

	@Test
	public void withPrettyStream() throws IOException {
		ImmutableComposite2 c = new ImmutableComposite2(
				new int[] { 1, -1, -0 },
				Arrays.asList("abc", "def", null, "ghi"),
				Double.parseDouble("123.456")
		);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, new PrettifyOutputStream(os));
		Assert.assertEquals(
				"{\n" +
						"  \"x\": [\n" +
						"    1,\n" +
						"    -1,\n" +
						"    0\n" +
						"  ],\n" +
						"  \"s\": [\n" +
						"    \"abc\",\n" +
						"    \"def\",\n" +
						"    null,\n" +
						"    \"ghi\"\n" +
						"  ],\n" +
						"  \"d\": 123.456\n" +
						"}", os.toString());
	}
}
