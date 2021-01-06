package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;
import java.math.BigDecimal;

@CompiledJson
public class ByteArrayConverter {
	@JsonAttribute(converter = Bytes.class)
	public byte[] bytes;

	public static abstract class Bytes {
		public static final JsonReader.ReadObject<byte[]> JSON_READER = new JsonReader.ReadObject<byte[]>() {
			public byte[] read(JsonReader reader) throws IOException {
				return null;
			}
		};
		public static final JsonWriter.WriteObject<byte[]> JSON_WRITER = new JsonWriter.WriteObject<byte[]>() {
			public void write(JsonWriter writer, byte[] value) {
			}
		};
	}
}
