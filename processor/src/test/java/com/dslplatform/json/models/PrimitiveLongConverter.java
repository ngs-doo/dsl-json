package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;

@CompiledJson
public class PrimitiveLongConverter {
	public long i;
	public Long j;

	@JsonConverter(target = long.class)
	public static abstract class LongConverter {
		public static long read(JsonReader reader) throws IOException {
			return NumberConverter.deserializeLong(reader);
		}

		public static final void write(JsonWriter writer, long value) {
			NumberConverter.serialize(value, writer);
		}
	}

	@JsonConverter(target = Long.class)
	public static abstract class NullableLongConverter {
		public static Long read(JsonReader reader) throws IOException {
			return NumberConverter.deserializeLong(reader);
		}

		public static void write(JsonWriter writer, Long value) {
			NumberConverter.serialize(value, writer);
		}
	}
}
