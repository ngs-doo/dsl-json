package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NonFinalNonEmptyCtors {

	@CompiledJson
	public static class NonEmpty {
		private int x;

		public int x() {
			return x;
		}

		public void x(int x) {
			this.x = x;
		}

		public NonEmpty(int x) {
			this.x = x;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void roundtrip() throws IOException {
		NonEmpty c = new NonEmpty(5);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(c, os);
		Assert.assertEquals("{\"x\":5}", os.toString());
		NonEmpty res = dslJson.deserialize(NonEmpty.class, os.toByteArray(), os.size());
		Assert.assertEquals(c.x(), res.x());
	}
}
