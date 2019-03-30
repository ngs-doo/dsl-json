package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ConverterTest {

	@JsonConverter(target = ClosedClass.class)
	public static class ClosedClassConverter {
		public static final JsonReader.ReadObject<ClosedClass> JSON_READER = new JsonReader.ReadObject<ClosedClass>() {
			@Override
			public ClosedClass read(JsonReader reader) throws IOException {
				return ClosedClass.create(reader.readString());
			}
		};
		public static JsonWriter.WriteObject<ClosedClass> JSON_WRITER() {
			return new JsonWriter.WriteObject<ClosedClass>() {
				@Override
				public void write(JsonWriter writer, @Nullable ClosedClass value) {
					writer.writeString(value.value);
				}
			};
		}
	}

	public static class ClosedClass {
		public final String value;
		private ClosedClass(String value) {
			this.value = value;
		}
		static ClosedClass create(String value) {
			return new ClosedClass(value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ClosedClass)) return false;
			return value.equals(((ClosedClass)obj).value);
		}
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY,CompiledJson.Format.OBJECT})
	public static class Composite1 {
		@JsonAttribute(index = 1, converter = IntConverter.class)
		public int x;
		@JsonAttribute(index = 2)
		public List<String> s;
		@JsonAttribute(index = 3, converter = ClosedClassConverter.class)
		public ClosedClass d;
	}
	@CompiledJson(formats = {CompiledJson.Format.OBJECT,CompiledJson.Format.ARRAY})
	public static class Composite2 {
		@JsonAttribute(index = 1, converter = IntConverter.class)
		public int x;
		@JsonAttribute(index = 2)
		public List<String> s;
		@JsonAttribute(index = 3)
		public ClosedClass d;
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY,CompiledJson.Format.OBJECT})
	public static class ImmutableComposite1 {
		@JsonAttribute(index = 1, converter = IntConverter.class)
		public final int x;
		@JsonAttribute(index = 2)
		public final List<String> s;
		@JsonAttribute(index = 3, converter = ClosedClassConverter.class)
		public final ClosedClass d;

		public ImmutableComposite1(int x, List<String> s, ClosedClass d) {
			this.x = x;
			this.s = s;
			this.d = d;
		}
	}
	@CompiledJson(formats = {CompiledJson.Format.OBJECT,CompiledJson.Format.ARRAY})
	public static class ImmutableComposite2 {
		@JsonAttribute(index = 1, converter = IntConverter.class)
		public final int x;
		@JsonAttribute(index = 2)
		public final List<String> s;
		@JsonAttribute(index = 3)
		public final ClosedClass d;

		public ImmutableComposite2(int x, List<String> s, ClosedClass d) {
			this.x = x;
			this.s = s;
			this.d = d;
		}
	}

	public static abstract class IntConverter {
		public static JsonReader.ReadObject<Integer> getJSON_READER() {
			return NumberConverter.INT_READER;
		}
		public static JsonWriter.WriteObject<Integer> JSON_WRITER() {
			return NumberConverter.INT_WRITER;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>().allowArrayFormat(true).includeServiceLoader());

	@Test
	public void firstArrayThenObjectForEmptyCtor() throws IOException {
		Composite1 c = new Composite1();
		c.d = ClosedClass.create("123.456");
		c.s = Arrays.asList("abc", "def", null, "ghi");
		c.x = -1;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("[-1,[\"abc\",\"def\",null,\"ghi\"],\"123.456\"]", os.toString());
		Composite1 res1 = dslJson.deserialize(Composite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res1.d);
		Assert.assertEquals(c.s, res1.s);
		Assert.assertEquals(c.x, res1.x);
		Composite2 res2 = dslJson.deserialize(Composite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res2.d);
		Assert.assertEquals(c.s, res2.s);
		Assert.assertEquals(c.x, res2.x);
	}

	@Test
	public void firstObjectThenArrayForEmptyCtor() throws IOException {
		Composite2 c = new Composite2();
		c.d = ClosedClass.create("123.456");
		c.s = Arrays.asList("abc", "def", null, "ghi");
		c.x = -1;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("{\"x\":-1,\"s\":[\"abc\",\"def\",null,\"ghi\"],\"d\":\"123.456\"}", os.toString());
		Composite1 res1 = dslJson.deserialize(Composite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res1.d);
		Assert.assertEquals(c.s, res1.s);
		Assert.assertEquals(c.x, res1.x);
		Composite2 res2 = dslJson.deserialize(Composite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res2.d);
		Assert.assertEquals(c.s, res2.s);
		Assert.assertEquals(c.x, res2.x);
	}

	@Test
	public void firstArrayThenObjectForNonEmptyCtor() throws IOException {
		ImmutableComposite1 c = new ImmutableComposite1(
				-1,
				Arrays.asList("abc", "def", null, "ghi"),
				ClosedClass.create("123.456")
		);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("[-1,[\"abc\",\"def\",null,\"ghi\"],\"123.456\"]", os.toString());
		ImmutableComposite1 res1 = dslJson.deserialize(ImmutableComposite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res1.d);
		Assert.assertEquals(c.s, res1.s);
		Assert.assertEquals(c.x, res1.x);
		ImmutableComposite2 res2 = dslJson.deserialize(ImmutableComposite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res2.d);
		Assert.assertEquals(c.s, res2.s);
		Assert.assertEquals(c.x, res2.x);
	}

	@Test
	public void firstObjectThenArrayForNonEmptyCtor() throws IOException {
		ImmutableComposite2 c = new ImmutableComposite2(
				-1,
				Arrays.asList("abc", "def", null, "ghi"),
				ClosedClass.create("123.456")
		);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("{\"x\":-1,\"s\":[\"abc\",\"def\",null,\"ghi\"],\"d\":\"123.456\"}", os.toString());
		ImmutableComposite1 res1 = dslJson.deserialize(ImmutableComposite1.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res1.d);
		Assert.assertEquals(c.s, res1.s);
		Assert.assertEquals(c.x, res1.x);
		ImmutableComposite2 res2 = dslJson.deserialize(ImmutableComposite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res2.d);
		Assert.assertEquals(c.s, res2.s);
		Assert.assertEquals(c.x, res2.x);
	}

	@Test
	public void cantChangeJsonConverter() throws IOException {
		DslJson<Object> changedDslJson = new DslJson<>(new DslJson.Settings<>().allowArrayFormat(true).includeServiceLoader());
		changedDslJson.registerWriter(ClosedClass.class, (w, v) -> w.writeNull());
		Composite2 c = new Composite2();
		c.d = ClosedClass.create("123.456");
		c.s = Arrays.asList("abc", "def", null, "ghi");
		c.x = -1;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		changedDslJson.serialize(c, os);
		Assert.assertEquals("{\"x\":-1,\"s\":[\"abc\",\"def\",null,\"ghi\"],\"d\":\"123.456\"}", os.toString());
		Composite2 res2 = changedDslJson.deserialize(Composite2.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.d, res2.d);
		Assert.assertEquals(c.s, res2.s);
		Assert.assertEquals(c.x, res2.x);
	}

	@CompiledJson
	public static class NullChecks{
		@JsonAttribute(converter = CustomIntConverter.class, nullable = false)
		public Integer x;
	}

	public static class CustomIntConverter {
		public static final JsonReader.ReadObject<Integer> JSON_READER = null;
		public static final JsonWriter.WriteObject<Integer> JSON_WRITER = null;
	}

	@Test
	public void nullChecksOnConverter() throws IOException {
		NullChecks nc = new NullChecks();
		nc.x = null;
		try {
			dslJson.serialize(nc, new ByteArrayOutputStream());
			Assert.fail("Expecting exception");
		} catch (ConfigurationException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 'x' is not allowed to be null"));
		}
		try {
			byte[] bytes = "{\"x\":null}".getBytes(StandardCharsets.UTF_8);
			dslJson.deserialize(NullChecks.class, bytes, bytes.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 'x' is not allowed to be null"));
		}
	}
}
