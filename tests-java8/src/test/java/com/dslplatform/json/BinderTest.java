package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BinderTest {
	@JsonConverter(target = BindingClass.class)
	public static class BindingClassConverter {
		public static final JsonReader.ReadObject<BindingClass> JSON_READER = reader -> {
			BindingClass inst = new BindingClass();
			inst.value = reader.readString();
			return inst;
		};
		public static JsonWriter.WriteObject<BindingClass> JSON_WRITER = (writer, value) -> writer.writeString(value.value);
		public static final JsonReader.BindObject<BindingClass> JSON_BINDER = (reader, inst) -> {
			inst.value = reader.readString();
			return inst;
		};
	}

	@CompiledJson
	public static class WrapperClass {
		public String a;
		@JsonAttribute(converter = BindingClassConverter.class)
		public BindingClass b = new BindingClass();
	}

	public static class BindingClass {
		public String value;
	}

	private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>().allowArrayFormat(true).includeServiceLoader());

	@Test
	public void bindWorks() throws IOException {
		WrapperClass wc = new WrapperClass();
		BindingClass internal = wc.b;
		{
			byte[] input = "{\"a\":\"abc\", \"b\":\"value\"}".getBytes(StandardCharsets.UTF_8);
			JsonReader<Object> reader = dslJson.newReader(input);
			reader.getNextToken();
			dslJson.tryFindBinder(WrapperClass.class).bind(reader, wc);
			Assert.assertEquals("abc", wc.a);
			Assert.assertEquals("value", wc.b.value);
			Assert.assertSame(internal, wc.b);
		}
		{
			byte[] input = "{\"a\":\"abc2\", \"b\":\"value2\"}".getBytes(StandardCharsets.UTF_8);
			JsonReader<Object> reader = dslJson.newReader(input);
			reader.getNextToken();
			dslJson.tryFindBinder(WrapperClass.class).bind(reader, wc);
			Assert.assertEquals("abc2", wc.a);
			Assert.assertEquals("value2", wc.b.value);
			Assert.assertSame(internal, wc.b);
		}
	}

	@Test
	public void readWorks() throws IOException {
		byte[] input = "{\"a\":\"abc\", \"b\":\"value\"}".getBytes(StandardCharsets.UTF_8);
		WrapperClass wc = dslJson.deserialize(WrapperClass.class, input, input.length);
		Assert.assertEquals("abc", wc.a);
		Assert.assertEquals("value", wc.b.value);
	}

	@Test
	public void nestedBindWorks() throws IOException {
		byte[] input = "{\"firstname\":\"me\", \"surname\":\"for-real\",\"age\":42}".getBytes(StandardCharsets.UTF_8);
		MutablePerson wc = dslJson.deserialize(MutablePerson.class, input, input.length);
		Assert.assertEquals("me", wc.firstname.value());
		Assert.assertEquals("for-real", wc.surname.value());
		Assert.assertEquals(42, wc.age);
		JsonWriter writer = dslJson.newWriter();
		dslJson.serialize(writer, wc);
		String output = writer.toString();
		Assert.assertTrue(output.contains("\"firstname\":\"me\""));
		Assert.assertTrue(output.contains("\"surname\":\"for-real\""));
		Assert.assertTrue(output.contains("\"age\":42"));
	}
}