package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PrimitivesTest {

	@CompiledJson
	public static class IntPrimitive {
		public int x;
	}

	@CompiledJson
	public static class ImmutableIntPrimitive {
		public final int x;

		public ImmutableIntPrimitive(int x) {
			this.x = x;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void cantHaveNullOnPrimitive() throws IOException {
		byte[] input = "{\"x\":null}".getBytes("UTF-8");
		try {
			dslJson.deserialize(IntPrimitive.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Error parsing number at position: 5"));
		}
	}

	@Test
	public void cantHaveNullOnImmutablePrimitive() throws IOException {
		byte[] input = "{\"x\":null}".getBytes("UTF-8");
		try {
			dslJson.deserialize(ImmutableIntPrimitive.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Error parsing number at position: 5"));
		}
	}

	@CompiledJson
	public static class FloatPrimitive {
		@JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.NON_DEFAULT)
		public float f1;
		@JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
		public float f2;
	}

	@Test
	public void noDefaultValueForFloat() throws IOException {
		FloatPrimitive fp = new FloatPrimitive();
		fp.f1 = 0;
		fp.f2 = 0;
		DslJson<Object> minimal = new DslJson<>(new DslJson.Settings<>().skipDefaultValues(true).includeServiceLoader());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		minimal.serialize(fp, os);
		Assert.assertEquals("{\"f2\":0.0}", os.toString("UTF-8"));
		FloatPrimitive res = minimal.deserialize(FloatPrimitive.class, os.toByteArray(), os.size());
		Assert.assertEquals(fp.f1, res.f1, 0);
		Assert.assertEquals(fp.f2, res.f2, 0);
	}
}
