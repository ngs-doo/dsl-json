package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class OptionalTest {

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class Composite {
		@JsonAttribute(index = 1)
		public OptionalInt oi = OptionalInt.empty();
		@JsonAttribute(index = 2)
		public Optional<String> os = Optional.empty();
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class ImmutableComposite {
		@JsonAttribute(index = 1)
		public final OptionalInt oi;
		@JsonAttribute(index = 2)
		public final Optional<String> os;

		public ImmutableComposite(OptionalInt oi, Optional<String> os) {
			this.oi = oi;
			this.os = os;
		}
	}

	private final DslJson<Object> dslJsonArray = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private final DslJson<Object> dslJsonObject = new DslJson<>(Settings.withRuntime().allowArrayFormat(false).includeServiceLoader());
	private final DslJson<Object> dslJsonMinimal = new DslJson<>(Settings.withRuntime().allowArrayFormat(false).skipDefaultValues(true).includeServiceLoader());

	@Test
	public void objectRoundtrip() throws IOException {
		Composite c = new Composite();
		c.oi = OptionalInt.empty();
		c.os = Optional.empty();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[null,null]", os.toString());
		Composite res = dslJsonArray.deserialize(Composite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), res.oi);
		Assert.assertEquals(Optional.empty(), res.os);
		os.reset();
		dslJsonObject.serialize(c, os);
		Assert.assertEquals("{\"oi\":null,\"os\":null}", os.toString());
		res = dslJsonObject.deserialize(Composite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), res.oi);
		Assert.assertEquals(Optional.empty(), res.os);
		os.reset();
		dslJsonMinimal.serialize(c, os);
		Assert.assertEquals("{}", os.toString());
		res = dslJsonMinimal.deserialize(Composite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), res.oi);
		Assert.assertEquals(Optional.empty(), res.os);
	}

	@Test
	public void immutableRoundtrip() throws IOException {
		ImmutableComposite c = new ImmutableComposite(
				OptionalInt.empty(),
				Optional.empty()
		);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[null,null]", os.toString());
		ImmutableComposite res = dslJsonArray.deserialize(ImmutableComposite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), res.oi);
		Assert.assertEquals(Optional.empty(), res.os);
		os.reset();
		dslJsonObject.serialize(c, os);
		Assert.assertEquals("{\"oi\":null,\"os\":null}", os.toString());
		res = dslJsonObject.deserialize(ImmutableComposite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), res.oi);
		Assert.assertEquals(Optional.empty(), res.os);
		os.reset();
		dslJsonMinimal.serialize(c, os);
		Assert.assertEquals("{}", os.toString());
		res = dslJsonMinimal.deserialize(ImmutableComposite.class, os.toByteArray(), os.size());
		Assert.assertEquals(OptionalInt.empty(), res.oi);
		Assert.assertEquals(Optional.empty(), res.os);
	}

	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class OptionInCollection {
		@JsonAttribute(index = 1)
		public List<OptionalInt> l1;
		@JsonAttribute(index = 2)
		public ArrayList<Optional<String>> l2;
		@JsonAttribute(index = 3)
		public Set<Optional<String>> l3;
		@JsonAttribute(index = 4)
		public OptionalDouble[] l4;
	}

	@Test
	public void withCollections() throws IOException {
		OptionInCollection c = new OptionInCollection();
		c.l1 = Arrays.asList(OptionalInt.empty(), null, OptionalInt.of(3));
		c.l2 = new ArrayList<>(Arrays.asList(null, Optional.of("abc"), Optional.empty()));
		c.l3 = new LinkedHashSet<>(Arrays.asList(null, Optional.of("abc"), Optional.empty()));
		c.l4 = new OptionalDouble[] { OptionalDouble.empty(), OptionalDouble.of(5), null };
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(c, os);
		Assert.assertEquals("[[null,null,3],[null,\"abc\",null],[null,\"abc\",null],[null,5.0,null]]", os.toString());
		OptionInCollection res = dslJsonArray.deserialize(OptionInCollection.class, os.toByteArray(), os.size());
		Assert.assertEquals(Arrays.asList(OptionalInt.empty(), OptionalInt.empty(), OptionalInt.of(3)), res.l1);
		Assert.assertEquals(Arrays.asList(Optional.empty(), Optional.of("abc"), Optional.empty()), res.l2);
		Assert.assertEquals(new LinkedHashSet<>(Arrays.asList(Optional.empty(), Optional.of("abc"), Optional.empty())), res.l3);
		Assert.assertArrayEquals(new OptionalDouble[] { OptionalDouble.empty(), OptionalDouble.of(5), OptionalDouble.empty() }, res.l4);
		os.reset();
		dslJsonObject.serialize(c, os);
		Assert.assertEquals("{\"l1\":[null,null,3],\"l2\":[null,\"abc\",null],\"l3\":[null,\"abc\",null],\"l4\":[null,5.0,null]}", os.toString());
		res = dslJsonObject.deserialize(OptionInCollection.class, os.toByteArray(), os.size());
		Assert.assertEquals(Arrays.asList(OptionalInt.empty(), OptionalInt.empty(), OptionalInt.of(3)), res.l1);
		Assert.assertEquals(Arrays.asList(Optional.empty(), Optional.of("abc"), Optional.empty()), res.l2);
		Assert.assertEquals(new LinkedHashSet<>(Arrays.asList(Optional.empty(), Optional.of("abc"), Optional.empty())), res.l3);
		Assert.assertArrayEquals(new OptionalDouble[] { OptionalDouble.empty(), OptionalDouble.of(5), OptionalDouble.empty() }, res.l4);
		os.reset();
		dslJsonMinimal.serialize(c, os);
		Assert.assertEquals("{\"l1\":[null,null,3],\"l2\":[null,\"abc\",null],\"l3\":[null,\"abc\",null],\"l4\":[null,5.0,null]}", os.toString());
		res = dslJsonMinimal.deserialize(OptionInCollection.class, os.toByteArray(), os.size());
		Assert.assertEquals(Arrays.asList(OptionalInt.empty(), OptionalInt.empty(), OptionalInt.of(3)), res.l1);
		Assert.assertEquals(Arrays.asList(Optional.empty(), Optional.of("abc"), Optional.empty()), res.l2);
		Assert.assertEquals(new LinkedHashSet<>(Arrays.asList(Optional.empty(), Optional.of("abc"), Optional.empty())), res.l3);
		Assert.assertArrayEquals(new OptionalDouble[] { OptionalDouble.empty(), OptionalDouble.of(5), OptionalDouble.empty() }, res.l4);
	}
}
