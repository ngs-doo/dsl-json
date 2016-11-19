package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;

@CompiledJson
public class PrimitivePropertyConverter {
	@JsonAttribute(converter = FormatInt2.class)
	public int i;

	public static abstract class FormatInt2 {
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
