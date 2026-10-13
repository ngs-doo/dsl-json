package com.dslplatform.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MixedDataTest {

	@CompiledJson
	public static class Message {
		public String type;
		@JsonAttribute(converter = DataAsArrayOrSingle.class)
		public List<Data> data;
	}

	@CompiledJson
	public static class Data {
		public String name;
	}

	public static class DataAsArrayOrSingle {
		private static JsonReader.ReadObject<Data> jReader;
		private static JsonWriter.WriteObject<Data> jWriter;
		public static void setup(DslJson json) {
			jReader = json.tryFindReader(Data.class);
			jWriter = json.tryFindWriter(Data.class);
		}
		public static final JsonReader.ReadObject<List<Data>> JSON_READER = reader -> {
			if (reader.last() == '{') {
				return Arrays.asList(jReader.read(reader));
			}
			return reader.readCollection(jReader);
		};
		public static JsonWriter.WriteObject<List<Data>> JSON_WRITER = (writer, value) -> {
			if (value != null && value.size() == 1) {
				jWriter.write(writer, value.get(0));
			} else {
				writer.serialize(value, jWriter);
			}
		};
	}

	private static final DslJson<Object> json = new DslJson<>();
	static {
		DataAsArrayOrSingle.setup(json);
	}

	private Message deserialize(String input) throws IOException {
		byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
		return json.deserialize(Message.class, bytes, bytes.length);
	}

	@Test
	public void shouldDeserializeArrayOfObjects() throws IOException {
		Message message = deserialize(
				"{\"type\":\"snapshot\",\"data\":[" +
						"{\"name\":\"John\"}," +
						"{\"name\":\"Jane\"}" +
						"]}"
		);

		assertEquals("snapshot", message.type);
		assertNotNull(message.data);
		assertEquals(2, message.data.size());
		assertEquals("John", message.data.get(0).name);
		assertEquals("Jane", message.data.get(1).name);
	}

	@Test
	public void shouldDeserializeSingleObject() throws IOException {
		Message message = deserialize(
				"{\"type\":\"update\",\"data\":{\"name\":\"John\"}}"
		);

		assertEquals("update", message.type);
		assertNotNull(message.data);
		assertEquals(1, message.data.size());
		assertEquals("John", message.data.get(0).name);
	}


	@Test
	public void shouldSerializeArrayOfObjects() throws IOException {
		Message message = new Message();
		message.type = "snapshot";

		Data john = new Data();
		john.name = "John";

		Data jane = new Data();
		jane.name = "Jane";

		message.data = Arrays.asList(new Data[] { john, jane });
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		json.serialize(message, os);
		String actual = os.toString();

		Message result = deserialize(actual);

		assertEquals("snapshot", result.type);
		assertEquals(2, result.data.size());
		assertEquals("John", result.data.get(0).name);
		assertEquals("Jane", result.data.get(1).name);
	}

	@Test
	public void shouldSerializeSingleEntryAsArray() throws IOException {
		Message message = new Message();
		message.type = "update";

		Data john = new Data();
		john.name = "John";
		message.data = Arrays.asList(new Data[] { john });

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		json.serialize(message, os);
		String actual = os.toString();

		Message result = deserialize(actual);

		assertEquals(1, result.data.size());
		assertEquals("John", result.data.get(0).name);
	}

	@Test
	public void shouldSerializeEmptyArray() throws IOException {
		Message message = new Message();
		message.type = "snapshot";
		message.data = new ArrayList<>();

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		json.serialize(message, os);
		String actual = os.toString();

		Message result = deserialize(actual);

		assertNotNull(result.data);
		assertEquals(0, result.data.size());
	}
}