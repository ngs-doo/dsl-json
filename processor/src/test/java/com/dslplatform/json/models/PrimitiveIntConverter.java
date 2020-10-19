package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;
import java.math.BigDecimal;

@CompiledJson
public class PrimitiveIntConverter {
	public int i;
	public Integer j;

	@JsonConverter(target = int.class)
	public static abstract class IntConverter {
		public static final JsonReader.ReadObject<Integer> JSON_READER = new JsonReader.ReadObject<Integer>() {
			public Integer read(JsonReader reader) throws IOException {
				return NumberConverter.deserializeInt(reader);
			}
		};
		public static final JsonWriter.WriteObject<Integer> JSON_WRITER = new JsonWriter.WriteObject<Integer>() {
			public void write(JsonWriter writer, Integer value) {
				NumberConverter.serialize(value, writer);
			}
		};
	}

	@JsonConverter(target = Integer.class)
	public static abstract class IntegerConverter {
		public static final JsonReader.ReadObject<Integer> JSON_READER = new JsonReader.ReadObject<Integer>() {
			public Integer read(JsonReader reader) throws IOException {
				return NumberConverter.deserializeInt(reader);
			}
		};
		public static final JsonWriter.WriteObject<Integer> JSON_WRITER = new JsonWriter.WriteObject<Integer>() {
			public void write(JsonWriter writer, Integer value) {
				NumberConverter.serialize(value, writer);
			}
		};
	}
}
