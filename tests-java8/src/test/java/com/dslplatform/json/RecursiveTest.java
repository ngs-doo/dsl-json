package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class RecursiveTest {

	@CompiledJson
	public static class Recursive {
		@NotNull
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

	@CompiledJson
	public static class RecursiveList {
		@JsonAttribute(index = 1)
		public int x;
		@JsonAttribute(index = 2)
		public List<RecursiveList> rl;
	}

	@CompiledJson
	public static class RecursiveAll {
		public RecursiveAll r1;
		public List<RecursiveAll> r2;
		public Set<RecursiveAll> r3;
		public Map<String, RecursiveAll> r4;
		public Map<String, List<RecursiveAll>[]>[] r5;
		public List<RecursiveAll[]>[] r6;
	}

	@CompiledJson
	public static class RecursiveWithWrapper {
		public RecursiveListWrapper r = new RecursiveListWrapper();
	}

	public static class RecursiveListWrapper extends ArrayList<RecursiveList> {
	}

	private final DslJson<Object> dslJson = new DslJson<>();
	private final DslJson<Object> dslJsonRuntime = new DslJson<>(Settings.basicSetup());

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
				throw reader.newParseErrorAt("Unable to find recursive with id: " + id, 0);
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

	@Test
	public void nonEmptyList() throws IOException {
		RecursiveList ra = new RecursiveList();
		ra.x = 5;
		RecursiveList rb = new RecursiveList();
		rb.x = 7;
		ra.rl = Arrays.asList(rb, null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(ra, os);
		Assert.assertEquals("{\"x\":5,\"rl\":[{\"x\":7,\"rl\":null},null]}", os.toString());
		RecursiveList res = dslJson.deserialize(RecursiveList.class, os.toByteArray(), os.size());
		Assert.assertEquals(ra.x, res.x);
		Assert.assertEquals(2, res.rl.size());
		Assert.assertNull(res.rl.get(1));
		Assert.assertEquals(7, res.rl.get(0).x);
	}

	//TODO: support for custom lists
	@Ignore
	@Test
	public void recursiveWrapperWithRuntime() throws IOException {
		RecursiveWithWrapper ra = new RecursiveWithWrapper();
		RecursiveList rs = new RecursiveList();
		rs.x = 5;
		ra.r.add(rs);
		ra.r.add(null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonRuntime.serialize(ra, os);
		Assert.assertEquals("{\"r\":[{\"x\":5,\"rl\":null},null]}", os.toString());
		RecursiveWithWrapper res = dslJson.deserialize(RecursiveWithWrapper.class, os.toByteArray(), os.size());
		Assert.assertEquals(2, res.r.size());
		Assert.assertNull(res.r.get(1));
		Assert.assertEquals(5, res.r.get(0).x);
	}
}
