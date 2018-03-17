package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

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
}
