package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class SkippingTest {

	public static class Single {
		public int x;
	}

	public static class SingleImmutable {
		public final long x;

		public SingleImmutable(long x) {
			this.x = x;
		}
	}

	private final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime());

	@Test
	public void canSkipOverObject1() throws IOException {
		byte[] input = "{\"x\":1,\"a\":2}".getBytes("UTF-8");
		Single s = dslJson.deserialize(Single.class, input, input.length);
		Assert.assertEquals(1, s.x);
	}

	@Test
	public void canSkipOverObject2() throws IOException {
		byte[] input = "{\"a\":1,\"x\":2}".getBytes("UTF-8");
		Single s = dslJson.deserialize(Single.class, input, input.length);
		Assert.assertEquals(2, s.x);
	}

	@Test
	public void canSkipOverObject3() throws IOException {
		byte[] input = "{\"a\":1,\"b\":3,\"x\":2}".getBytes("UTF-8");
		Single s = dslJson.deserialize(Single.class, input, input.length);
		Assert.assertEquals(2, s.x);
	}

	@Test
	public void canSkipOverObject4() throws IOException {
		byte[] input = "{\"a\":1,\"b\":3,\"x\":2,\"c\":null}".getBytes("UTF-8");
		Single s = dslJson.deserialize(Single.class, input, input.length);
		Assert.assertEquals(2, s.x);
	}

	@Test
	public void noSkippingOverObject5() throws IOException {
		byte[] input = "{\"x\":2}".getBytes("UTF-8");
		Single s = dslJson.deserialize(Single.class, input, input.length);
		Assert.assertEquals(2, s.x);
	}

	@Test
	public void doublePropertyObject6() throws IOException {
		byte[] input = "{\"x\":2,\"x\":4}".getBytes("UTF-8");
		Single s = dslJson.deserialize(Single.class, input, input.length);
		Assert.assertEquals(4, s.x);
	}

	@Test
	public void canSkipOverImmutable1() throws IOException {
		byte[] input = "{\"x\":1,\"a\":2}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(1, s.x);
	}

	@Test
	public void canSkipOverImmutable2() throws IOException {
		byte[] input = "{\"a\":1,\"x\":2}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(2, s.x);
	}

	@Test
	public void canSkipOverImmutable3() throws IOException {
		byte[] input = "{\"a\":1,\"b\":3,\"x\":2}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(2, s.x);
	}

	@Test
	public void canSkipOverImmutable4() throws IOException {
		byte[] input = "{\"a\":1,\"b\":3,\"x\":2,\"c\":null}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(2, s.x);
	}

	@Test
	public void noSkippingOverImmutable5() throws IOException {
		byte[] input = "{\"x\":2}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(2, s.x);
	}

	@Test
	public void doublePropertyImmutable6() throws IOException {
		byte[] input = "{\"x\":2,\"x\":4}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(4, s.x);
	}

	public static class S {
		public String s;
	}

	@Test
	public void whenSkippingOverAttributesCopeWithDoubleQuoteEscaping() throws IOException {
		byte[] input = "{\"s\":\"s\",\"s\\\"xy\":3}".getBytes("UTF-8");
		S s = dslJson.deserialize(S.class, input, input.length);
		Assert.assertEquals("s", s.s);
	}
}
