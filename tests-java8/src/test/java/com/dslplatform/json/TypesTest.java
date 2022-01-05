package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TypesTest {

	@CompiledJson
	public static class All {
		public Boolean b;
	}

	private final DslJson<Object> dslJsonFull = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private final DslJson<Object> dslJsonMinimal = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).skipDefaultValues(true).includeServiceLoader());

	private final DslJson<Object>[] dslJsons = new DslJson[]{dslJsonFull, dslJsonMinimal};

	@Test
	public void compare() throws IOException {
		for (DslJson<Object> dslJson : dslJsons) {
			All a = new All();
			a.b = true;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dslJson.serialize(a, os);
			All res = dslJson.deserialize(All.class, os.toByteArray(), os.size());
			Assert.assertEquals(a.b, res.b);
		}
	}

	@CompiledJson
	public static class Zeros {
		@JsonAttribute(nullable = false)
		public Boolean b;
		@JsonAttribute(nullable = false)
		public Short s;
		@JsonAttribute(nullable = false)
		public Integer i;
		@JsonAttribute(nullable = false)
		public Long l;
		@JsonAttribute(nullable = false)
		public Double d;
		@JsonAttribute(nullable = false)
		public Float f;

		public Zeros(Boolean b, Short s, Integer i, Long l, Double d, Float f) {
			this.b = b;
			this.s = s;
			this.i = i;
			this.l = l;
			this.d = d;
			this.f = f;
		}
	}

	@Test
	public void allZeros() throws IOException {
		Zeros z = dslJsonFull.deserialize(Zeros.class, new byte[]{'{', '}'}, 2);
		Assert.assertEquals(Boolean.FALSE, z.b);
		Assert.assertEquals(Short.valueOf((short) 0), z.s);
		Assert.assertEquals(Integer.valueOf(0), z.i);
		Assert.assertEquals(Long.valueOf(0L), z.l);
		Assert.assertEquals(Double.valueOf(0.0), z.d);
		Assert.assertEquals(Float.valueOf(0f), z.f);
	}

	@CompiledJson
	public static class BasicCollections {
		@JsonAttribute(nullable = false)
		public boolean[] b1;
		@JsonAttribute(nullable = false)
		public Boolean[] b2;
		@JsonAttribute(nullable = false)
		public List<Boolean> b3;
		@JsonAttribute(nullable = false)
		public Set<Boolean> b4;
		@JsonAttribute(nullable = false)
		public short[] s1;
		@JsonAttribute(nullable = false)
		public Short[] s2;
		@JsonAttribute(nullable = false)
		public List<Short> s3;
		@JsonAttribute(nullable = false)
		public Set<Short> s4;
		@JsonAttribute(nullable = false)
		public int[] i1;
		@JsonAttribute(nullable = false)
		public Integer[] i2;
		@JsonAttribute(nullable = false)
		public List<Integer> i3;
		@JsonAttribute(nullable = false)
		public Set<Integer> i4;
		@JsonAttribute(nullable = false)
		public long[] l1;
		@JsonAttribute(nullable = false)
		public Long[] l2;
		@JsonAttribute(nullable = false)
		public List<Long> l3;
		@JsonAttribute(nullable = false)
		public Set<Long> l4;
		@JsonAttribute(nullable = false)
		public double[] d1;
		@JsonAttribute(nullable = false)
		public Double[] d2;
		@JsonAttribute(nullable = false)
		public List<Double> d3;
		@JsonAttribute(nullable = false)
		public Set<Double> d4;
		@JsonAttribute(nullable = false)
		public float[] f1;
		@JsonAttribute(nullable = false)
		public Float[] f2;
		@JsonAttribute(nullable = false)
		public List<Float> f3;
		@JsonAttribute(nullable = false)
		public Set<Float> f4;

		public BasicCollections(
				boolean[] b1, Boolean[] b2, List<Boolean> b3, Set<Boolean> b4,
				short[] s1, Short[] s2, List<Short> s3, Set<Short> s4,
				int[] i1, Integer[] i2, List<Integer> i3, Set<Integer> i4,
				long[] l1, Long[] l2, List<Long> l3, Set<Long> l4,
				double[] d1, Double[] d2, List<Double> d3, Set<Double> d4,
				float[] f1, Float[] f2, List<Float> f3, Set<Float> f4) {
			this.b1 = b1;
			this.b2 = b2;
			this.b3 = b3;
			this.b4 = b4;
			this.s1 = s1;
			this.s2 = s2;
			this.s3 = s3;
			this.s4 = s4;
			this.i1 = i1;
			this.i2 = i2;
			this.i3 = i3;
			this.i4 = i4;
			this.l1 = l1;
			this.l2 = l2;
			this.l3 = l3;
			this.l4 = l4;
			this.d1 = d1;
			this.d2 = d2;
			this.d3 = d3;
			this.d4 = d4;
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	@Test
	public void emptyCollections() throws IOException {
		BasicCollections c = dslJsonFull.deserialize(BasicCollections.class, new byte[]{'{', '}'}, 2);
		Assert.assertEquals(0, c.b1.length);
		Assert.assertEquals(0, c.b2.length);
		Assert.assertEquals(0, c.b3.size());
		Assert.assertEquals(0, c.b4.size());
		Assert.assertEquals(0, c.s1.length);
		Assert.assertEquals(0, c.s2.length);
		Assert.assertEquals(0, c.s3.size());
		Assert.assertEquals(0, c.s4.size());
		Assert.assertEquals(0, c.i1.length);
		Assert.assertEquals(0, c.i2.length);
		Assert.assertEquals(0, c.i3.size());
		Assert.assertEquals(0, c.i4.size());
		Assert.assertEquals(0, c.l1.length);
		Assert.assertEquals(0, c.l2.length);
		Assert.assertEquals(0, c.l3.size());
		Assert.assertEquals(0, c.l4.size());
		Assert.assertEquals(0, c.d1.length);
		Assert.assertEquals(0, c.d2.length);
		Assert.assertEquals(0, c.d3.size());
		Assert.assertEquals(0, c.d4.size());
		Assert.assertEquals(0, c.f1.length);
		Assert.assertEquals(0, c.f2.length);
		Assert.assertEquals(0, c.f3.size());
		Assert.assertEquals(0, c.f4.size());
	}

	@CompiledJson
	public static class ComplexArrays {
		@JsonAttribute(nullable = false)
		public int[][] i1;
		@JsonAttribute(nullable = false)
		public Integer[][] i2;
		@JsonAttribute(nullable = false)
		public List<Integer>[] i3;
		@JsonAttribute(nullable = false)
		public List<int[]>[] i4;
		@JsonAttribute(nullable = false)
		public Map<Integer, List<int[]>>[] i5;

		public ComplexArrays(int[][] i1, Integer[][] i2, List<Integer>[] i3, List<int[]>[] i4, Map<Integer, List<int[]>>[] i5) {
			this.i1 = i1;
			this.i2 = i2;
			this.i3 = i3;
			this.i4 = i4;
			this.i5 = i5;
		}
	}

	@Test
	public void emptyArrays() throws IOException {
		ComplexArrays z = dslJsonFull.deserialize(ComplexArrays.class, new byte[]{'{', '}'}, 2);
		Assert.assertEquals(0, z.i1.length);
		Assert.assertEquals(0, z.i2.length);
		Assert.assertEquals(0, z.i3.length);
		Assert.assertEquals(0, z.i4.length);
		Assert.assertEquals(0, z.i5.length);
	}

	@CompiledJson
	public static class UUIDS {
		@JsonAttribute(nullable = false)
		public UUID u1;
		public UUID u2;
		public List<UUID> u3;
		public java.util.ArrayList<UUID> u4;
	}

	@CompiledJson
	public static class DateTimes {
		@JsonAttribute(nullable = false)
		public OffsetDateTime t1;
		public OffsetDateTime t2;
	}

	@CompiledJson
	public static class DateTimesWithCtor {
		@JsonAttribute(nullable = false)
		public OffsetDateTime t1;
		public OffsetDateTime t2;

		public DateTimesWithCtor(OffsetDateTime t1, OffsetDateTime t2) {
			this.t1 = t1;
			this.t2 = t2;
		}
	}

	@CompiledJson
	public static class DateTimesCollection {
		@JsonAttribute(nullable = false)
		public List<OffsetDateTime> t1;
		@JsonAttribute(nullable = false)
		public java.util.ArrayList<OffsetDateTime> t2;
		public List<OffsetDateTime> t3;
		public java.util.ArrayList<OffsetDateTime> t4;

		public DateTimesCollection(List<OffsetDateTime> t1, java.util.ArrayList<OffsetDateTime> t2, List<OffsetDateTime> t3, java.util.ArrayList<OffsetDateTime> t4) {
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			this.t4 = t4;
		}
	}

	@Test
	public void nonnullableWithDefaultIsNotMandatory() throws IOException {
		byte[] input = "{}".getBytes("UTF-8");
		UUIDS u = dslJsonFull.deserialize(UUIDS.class, input, input.length);
		Assert.assertEquals(new UUID(0L, 0L), u.u1);
		Assert.assertEquals(null, u.u2);
	}

	@Test
	public void nonnullableWithoutDefaultIsMandatoryWithEmptyCtor() throws IOException {
		byte[] input = "{}".getBytes("UTF-8");
		try {
			dslJsonFull.deserialize(DateTimes.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 't1' is not-nullable and doesn't have a default but was not found in JSON"));
		}
	}

	@Test
	public void nonnullableWithoutDefaultIsMandatoryWithoutEmptyCtor() throws IOException {
		byte[] input = "{}".getBytes("UTF-8");
		try {
			dslJsonFull.deserialize(DateTimesWithCtor.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 't1' is not-nullable and doesn't have a default but was not found in JSON"));
		}
	}

	@Test
	public void correctIndexOnSlowBindAtEndWithEmptyCtor() throws IOException {
		byte[] input = "{\"t2\":null}".getBytes("UTF-8");
		try {
			dslJsonFull.deserialize(DateTimes.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 't1' is not-nullable and doesn't have a default but was not found in JSON"));
		}
	}

	@Test
	public void correctIndexOnSlowBindAtEndWithoutEmptyCtor() throws IOException {
		byte[] input = "{\"t2\":null}".getBytes("UTF-8");
		try {
			dslJsonFull.deserialize(DateTimesWithCtor.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 't1' is not-nullable and doesn't have a default but was not found in JSON"));
		}
	}

	@Test
	public void correctIndexOnSlowBindWithExtraOnEmptyCtor() throws IOException {
		byte[] input = "{\"t2\":null,\"a\":1}".getBytes("UTF-8");
		try {
			dslJsonFull.deserialize(DateTimes.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 't1' is not-nullable and doesn't have a default but was not found in JSON"));
		}
	}

	@Test
	public void correctIndexOnSlowBindWithExtraWithoutEmptyCtor() throws IOException {
		byte[] input = "{\"t2\":null,\"a\":1}".getBytes("UTF-8");
		try {
			dslJsonFull.deserialize(DateTimesWithCtor.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 't1' is not-nullable and doesn't have a default but was not found in JSON"));
		}
	}

	@Test
	public void defaultOnUnsupportedCollections() throws IOException {
		byte[] input = "{}".getBytes("UTF-8");
		try {
			dslJsonFull.deserialize(DateTimesCollection.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 't2' is not-nullable and doesn't have a default but was not found in JSON"));
		}
	}

	@CompiledJson
	public static class Longs {
		public Long l1;
		@JsonAttribute(nullable = false)
		public Long l2;

		public Longs(Long l1, Long l2) {
			this.l1 = l1;
			this.l2 = l2;
		}
	}

	@Test
	public void emptyNullableLongs() throws IOException {
		Longs z = dslJsonFull.deserialize(Longs.class, new byte[]{'{', '}'}, 2);
		Assert.assertEquals(null, z.l1);
		Assert.assertEquals(Long.valueOf(0L), z.l2);
	}

	@Test
	public void actuallyNullableLongs() throws IOException {
		byte[] input = "{\"l1\":null}".getBytes("UTF-8");
		Longs z = dslJsonFull.deserialize(Longs.class, input, input.length);
		Assert.assertEquals(null, z.l1);
		Assert.assertEquals(Long.valueOf(0L), z.l2);
	}

	@Test
	public void errorInNullInLongs() throws IOException {
		byte[] input = "{\"l1\":null,\"l2\":null}".getBytes("UTF-8");
		try {
			dslJsonFull.deserialize(Longs.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (ParsingException ex) {
			Assert.assertTrue(ex.getMessage().contains("Property 'l2' is not allowed to be null at position: 20"));
		}
	}

	@CompiledJson
	public static class NonPrimitiveNulls {
		public Boolean b;
		public Short s;
		public Integer i;
		public Long l;
		public Double d;
		public Float f;

		public NonPrimitiveNulls(Boolean b, Short s, Integer i, Long l, Double d, Float f) {
			this.b = b;
			this.s = s;
			this.i = i;
			this.l = l;
			this.d = d;
			this.f = f;
		}
	}

	@Test
	public void allNonPrimitiveMissing() throws IOException {
		NonPrimitiveNulls z = dslJsonFull.deserialize(NonPrimitiveNulls.class, new byte[]{'{', '}'}, 2);
		Assert.assertNull(z.b);
		Assert.assertNull(z.s);
		Assert.assertNull(z.i);
		Assert.assertNull(z.l);
		Assert.assertNull(z.d);
		Assert.assertNull(z.f);
	}

	@Test
	public void allNonPrimitiveNulls() throws IOException {
		byte[] input = "{\"b\":null,\"s\":null,\"i\":null,\"l\":null,\"d\":null,\"f\":null}".getBytes("UTF-8");
		NonPrimitiveNulls z = dslJsonFull.deserialize(NonPrimitiveNulls.class, input, input.length);
		Assert.assertNull(z.b);
		Assert.assertNull(z.s);
		Assert.assertNull(z.i);
		Assert.assertNull(z.l);
		Assert.assertNull(z.d);
		Assert.assertNull(z.f);
	}

	@CompiledJson
	public static class NonNullable1 {

		private String s;

		@NonNull
		public String getS() { return s; }

		public NonNullable1(String s) {
			this.s = s;
		}
	}

	@CompiledJson
	public static class NonNullable2 {

		@NonNull
		private String s;

		public String getS() { return s; }

		public NonNullable2(String s) {
			this.s = s;
		}
	}

	@CompiledJson
	public static class NonNullable3 {

		private String s;

		@NotNull
		@JsonAttribute(converter = ConvertString.class, nullable = false)
		public String getS() { return s; }

		public NonNullable3(String s) {
			this.s = s;
		}

		public static class ConvertString {

			public static final JsonReader.ReadObject<String> JSON_READER = StringConverter.READER;
			public static final JsonWriter.WriteObject<@NotNull String> JSON_WRITER = StringConverter.WRITER;
		}
	}

	@CompiledJson
	public static class NonNullable4 {

		private String s;

		@JsonAttribute(converter = ConvertString.class, nullable = false)
		public String getS() { return s; }

		public NonNullable4(@NotNull String s) {
			this.s = s;
		}

		public static class ConvertString {

			public static final JsonReader.ReadObject<@NotNull String> JSON_READER = StringConverter.READER;
			public static final JsonWriter.WriteObject<String> JSON_WRITER = StringConverter.WRITER;
		}
	}

	public static class NonNullable5 {

		private String s;

		@NotNull
		public String getS() { return s; }

		private NonNullable5() {
		}

		@CompiledJson
		public static NonNullable5 factory(String s) {
			NonNullable5 res = new NonNullable5();
			res.s = s;
			return res;
		}
	}

	public static class NonNullable6 {

		private String s;

		@NonNull
		public String getS() { return s; }

		private NonNullable6() {
		}

		public static class Builder {

			private String s;

			public Builder setS(String s) {
				this.s = s;
				return this;
			}

			@CompiledJson
			public NonNullable6 build() {
				NonNullable6 res = new NonNullable6();
				res.s = s;
				return res;
			}
		}

		public static Builder builder() {
			return new Builder();
		}
	}

	@Test
	public void cantBeNull() throws IOException {
		byte[] input = "{\"s\":null}".getBytes("UTF-8");
		Class<?>[] nonNullable = {NonNullable1.class, NonNullable2.class, NonNullable3.class,
				NonNullable4.class, NonNullable5.class, NonNullable6.class};
		for(Class<?> signature : nonNullable) {
			try {
				dslJsonFull.deserialize(signature, input, input.length);
				Assert.fail("Expecting exception for " + signature);
			} catch (ParsingException ex) {
				Assert.assertEquals("Property 's' is not allowed to be null at position: 9, following: `{\"s\":null`, before: `}`", ex.getMessage());
			}
		}
	}

	@CompiledJson
	public static class Event {
		@JsonAttribute(nullable = true)
		public String nameNull;
		@JsonAttribute(nullable = false)
		public String nameNonNull;
		@JsonAttribute(nullable = true)
		public Source sourceNull;
		@JsonAttribute(nullable = false)
		public Source sourceNonNull;
	}
	public static class Source {
		public String name;
	}

	@Test
	public void nullableBehaviorWithMissing() throws IOException {
		Event e = dslJsonFull.deserialize(Event.class, "{}".getBytes(StandardCharsets.UTF_8), 2);
		Assert.assertNull(e.nameNull);
		Assert.assertNotNull(e.nameNonNull);
		Assert.assertNull(e.sourceNull);
		Assert.assertNotNull(e.sourceNonNull);
	}
}