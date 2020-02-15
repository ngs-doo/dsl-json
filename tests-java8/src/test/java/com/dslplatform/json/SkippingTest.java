package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SkippingTest {

	@CompiledJson
	public static class Single {
		public int x;
	}

	@CompiledJson
	public static class SingleImmutable {
		@JsonAttribute(name = "x")
		public final long y;

		public SingleImmutable(long y) {
			this.y = y;
		}
	}

	private static final DslJson<Object> dslJson = new DslJson<>();

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
		Assert.assertEquals(1, s.y);
	}

	@Test
	public void canSkipOverImmutable2() throws IOException {
		byte[] input = "{\"a\":1,\"x\":2}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(2, s.y);
	}

	@Test
	public void canSkipOverImmutable3() throws IOException {
		byte[] input = "{\"a\":1,\"b\":3,\"x\":2}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(2, s.y);
	}

	@Test
	public void canSkipOverImmutable4() throws IOException {
		byte[] input = "{\"a\":1,\"b\":3,\"x\":2,\"c\":null}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(2, s.y);
	}

	@Test
	public void noSkippingOverImmutable5() throws IOException {
		byte[] input = "{\"x\":2}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(2, s.y);
	}

	@Test
	public void doublePropertyImmutable6() throws IOException {
		byte[] input = "{\"x\":2,\"x\":4}".getBytes("UTF-8");
		SingleImmutable s = dslJson.deserialize(SingleImmutable.class, input, input.length);
		Assert.assertEquals(4, s.y);
	}

	private static JsonReader.BindObject<Unnesting> binder = dslJson.tryFindBinder(Unnesting.class);
	private static ThreadLocal<JsonReader> unnestingReader = ThreadLocal.withInitial(dslJson::newReader);

	@CompiledJson
	public static class Unnesting {
		public String a;
		public String b;
		@JsonAttribute(name = "c")
		public String anUnknownFieldName;
		public String getJsonString() { return null; }
		public void setJsonString(String value) throws IOException {
			JsonReader localReader = unnestingReader.get();
			byte[] input = value.getBytes(StandardCharsets.UTF_8);
			localReader.process(input, input.length);
			localReader.read();
			binder.bind(localReader, this);
		}
	}

	@Test
	public void nestedBinding() throws IOException {
		byte[] input = "{\"jsonString\":\"{\\\"a\\\": \\\"value1\\\", \\\"b\\\": \\\"value2\\\"}\",\"c\":\"Some string\"}".getBytes("UTF-8");
		Unnesting s = dslJson.deserialize(Unnesting.class, input, input.length);
		Assert.assertEquals("value1", s.a);
		Assert.assertEquals("value2", s.b);
		Assert.assertEquals("Some string", s.anUnknownFieldName);
	}
}
