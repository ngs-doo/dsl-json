package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class RecursiveTest {

	@CompiledJson
	public static class Recursive {
		public int x;
		public Recursive r;
	}

	@CompiledJson
	public static class ImmutableRecursive {
		public final int x;
		public final ImmutableRecursive r;

		public ImmutableRecursive(int x, ImmutableRecursive r) {
			this.x = x;
			this.r = r;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void nullObjectRoundtrip() throws IOException {
		Recursive r = new Recursive();
		r.x = 5;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(r, os);
		Assert.assertEquals("{\"x\":5,\"r\":null}", os.toString());
		Recursive res = dslJson.deserialize(Recursive.class, os.toByteArray(), os.size());
		Assert.assertEquals(r.x, res.x);
		Assert.assertEquals(r.r, res.r);
	}

	@Test
	public void oneLevelObjectRoundtrip() throws IOException {
		Recursive nested = new Recursive();
		nested.x = 6;
		Recursive r = new Recursive();
		r.x = 5;
		r.r = nested;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(r, os);
		Assert.assertEquals("{\"x\":5,\"r\":{\"x\":6,\"r\":null}}", os.toString());
		Recursive res = dslJson.deserialize(Recursive.class, os.toByteArray(), os.size());
		Assert.assertEquals(r.x, res.x);
		Assert.assertEquals(r.r.x, res.r.x);
		Assert.assertEquals(r.r.r, res.r.r);
	}

	@Test
	public void selfRecursionWillStackOverflowEmpty() throws IOException {
		Recursive r = new Recursive();
		r.x = 5;
		r.r = r;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			dslJson.serialize(r, os);
			Assert.fail("Exception expected");
		} catch (StackOverflowError ignore) {
		}
	}

	@Test
	public void nullImmutableRoundtrip() throws IOException {
		ImmutableRecursive r = new ImmutableRecursive(5, null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(r, os);
		Assert.assertEquals("{\"x\":5,\"r\":null}", os.toString());
		ImmutableRecursive res = dslJson.deserialize(ImmutableRecursive.class, os.toByteArray(), os.size());
		Assert.assertEquals(r.x, res.x);
		Assert.assertEquals(r.r, res.r);
	}

	@Test
	public void oneLevelImmutableRoundtrip() throws IOException {
		ImmutableRecursive nested = new ImmutableRecursive(6, null);
		ImmutableRecursive r = new ImmutableRecursive(5, nested);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(r, os);
		Assert.assertEquals("{\"x\":5,\"r\":{\"x\":6,\"r\":null}}", os.toString());
		ImmutableRecursive res = dslJson.deserialize(ImmutableRecursive.class, os.toByteArray(), os.size());
		Assert.assertEquals(r.x, res.x);
		Assert.assertEquals(r.r.x, res.r.x);
		Assert.assertEquals(r.r.r, res.r.r);
	}

	static class InstanceTracker {
		static ThreadLocal<Set<Integer>> encoding = ThreadLocal.withInitial(HashSet::new);
		static ThreadLocal<List<Recursive>> decoding = ThreadLocal.withInitial(ArrayList::new);
	}

	@Test
	public void customCyclesHandling() throws IOException {
		DslJson<Object> dslJson = new DslJson<>();
		final JsonWriter.WriteObject<Recursive> oldEncoder = dslJson.tryFindWriter(Recursive.class);
		final JsonReader.BindObject<Recursive> binder = dslJson.tryFindBinder(Recursive.class);
		dslJson.registerWriter(Recursive.class, (writer, value) -> {
			if (value == null) writer.writeNull();
			else if (InstanceTracker.encoding.get().add(value.x)) oldEncoder.write(writer, value);
			else NumberConverter.serialize(value.x, writer);
		});
		dslJson.registerReader(Recursive.class, reader -> {
			if (reader.wasNull()) return null;
			List<Recursive> parsed = InstanceTracker.decoding.get();
			if (reader.last() == '{') {
				Recursive instance = new Recursive();
				parsed.add(instance);
				binder.bind(reader, instance);
				return instance;
			} else {
				int id = NumberConverter.deserializeInt(reader);
				for (Recursive r : parsed) {
					if (r.x == id) return r;
				}
				throw new ParsingException("Unable to find recursive with id: " + id);
			}
		});
		Recursive r1 = new Recursive();
		Recursive r2 = new Recursive();
		r1.x = 5;
		r1.r = r2;
		r2.x = 6;
		r2.r = r1;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(r1, os);
		Assert.assertEquals("{\"x\":5,\"r\":{\"x\":6,\"r\":5}}", os.toString());
		Recursive res = dslJson.deserialize(Recursive.class, os.toByteArray(), os.size());
		Assert.assertEquals(r1.x, res.x);
		Assert.assertEquals(r1.r.x, res.r.x);
		Assert.assertEquals(res, res.r.r);
	}
}
