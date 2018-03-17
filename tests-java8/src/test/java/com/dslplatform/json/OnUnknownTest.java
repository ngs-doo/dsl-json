package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class OnUnknownTest {

	@CompiledJson(onUnknown = CompiledJson.Behavior.FAIL)
	public static class Single {
		public int x;
	}

	@CompiledJson(onUnknown = CompiledJson.Behavior.FAIL)
	public static class SingleImmutable {
		@JsonAttribute(name = "x")
		public final long y;

		public SingleImmutable(long y) {
			this.y = y;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void willFailOnUnknownEmpty1() throws IOException {
		byte[] input = "{\"x\":1,\"a\":2}".getBytes("UTF-8");
		try {
			dslJson.deserialize(Single.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("at position: 7"));
		}
	}

	@Test
	public void willFailOnUnknownEmpty2() throws IOException {
		byte[] input = "{\"a\":1,\"x\":2}".getBytes("UTF-8");
		try {
			dslJson.deserialize(Single.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Unknown property detected: 'a'"));
			Assert.assertTrue(ex.getMessage().contains("at position: 1"));
		}
	}

	@Test
	public void willFailOnUnknownNonEmpty1() throws IOException {
		byte[] input = "{\"x\":1,\"a\":2}".getBytes("UTF-8");
		try {
			dslJson.deserialize(SingleImmutable.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Unknown property detected: 'a'"));
			Assert.assertTrue(ex.getMessage().contains("at position: 7"));
		}
	}

	@Test
	public void willFailOnUnknownNonEmpty2() throws IOException {
		byte[] input = "{\"a\":1,\"x\":2}".getBytes("UTF-8");
		try {
			dslJson.deserialize(SingleImmutable.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Unknown property detected: 'a'"));
			Assert.assertTrue(ex.getMessage().contains("at position: 1"));
		}
	}
}