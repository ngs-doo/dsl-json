package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RecursiveArrayTest {

	@CompiledJson(formats = CompiledJson.Format.ARRAY)
	public static class Recursive {
		@JsonAttribute(index = 1)
		public int x;
		@JsonAttribute(index = 2)
		public Recursive r;
	}

	@CompiledJson(formats = CompiledJson.Format.ARRAY)
	public static class ImmutableRecursive {
		@JsonAttribute(index = 1)
		public final int x;
		@JsonAttribute(index = 2)
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
		Assert.assertEquals("[5,null]", os.toString());
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
		Assert.assertEquals("[5,[6,null]]", os.toString());
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
			Assert.fail("Expection expected");
		} catch (StackOverflowError ignore) {
		}
	}

	@Test
	public void nullImmutableRoundtrip() throws IOException {
		ImmutableRecursive r = new ImmutableRecursive(5, null);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(r, os);
		Assert.assertEquals("[5,null]", os.toString());
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
		Assert.assertEquals("[5,[6,null]]", os.toString());
		ImmutableRecursive res = dslJson.deserialize(ImmutableRecursive.class, os.toByteArray(), os.size());
		Assert.assertEquals(r.x, res.x);
		Assert.assertEquals(r.r.x, res.r.x);
		Assert.assertEquals(r.r.r, res.r.r);
	}
}
