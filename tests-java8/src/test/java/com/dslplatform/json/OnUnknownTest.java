package com.dslplatform.json;

import com.dslplatform.json.runtime.CollectionAnalyzer;
import com.dslplatform.json.runtime.ObjectAnalyzer;
import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

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

	@CompiledJson
	static class UnknownModel {
		public String key = "abc";
		public Object value = 42;
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

	@Test
	public void canWorkWithObjectType() throws IOException {
		byte[] input = "{\"key\":\"abc\",\"value\":2}".getBytes("UTF-8");
		try {
			dslJson.deserialize(UnknownModel.class, input, input.length);
			Assert.fail("Expecting exception");
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Unable to find reader for class java.lang.Object"));
		}
		try {
			dslJson.serialize(new UnknownModel(), new ByteArrayOutputStream());
			Assert.fail("Expecting exception");
		} catch (Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Unable to find writer for class java.lang.Object"));
		}
		DslJson<Object> dslUnknown = new DslJson<>(
				new DslJson.Settings<>()
						.resolveReader(Settings.UNKNOWN_READER)
						.resolveWriter(Settings.UNKNOWN_WRITER)
						.includeServiceLoader());
		UnknownModel um1 = dslUnknown.deserialize(UnknownModel.class, input, input.length);
		Assert.assertEquals("abc", um1.key);
		Assert.assertEquals(2L, um1.value);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslUnknown.serialize(new UnknownModel(), os);
		Assert.assertEquals("{\"key\":\"abc\",\"value\":42}", os.toString("UTF-8"));
	}
}