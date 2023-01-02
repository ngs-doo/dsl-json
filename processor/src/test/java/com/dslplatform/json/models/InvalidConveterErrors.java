package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;
import java.util.Date;

@CompiledJson
public class InvalidConveterErrors {
	public Date d;
	public Character c;
	public Short s;

	@JsonConverter(target = int.class)
	public static abstract class IntConverter {
	}

	@JsonConverter(target = Character.class)
	public static abstract class CharConverter {
		public static final JsonReader.ReadObject<Character> JSON_READER = new JsonReader.ReadObject<Character>() {
			@Nullable
			public Character read(JsonReader reader) throws IOException {
				return null;
			}
		};
	}

	@JsonConverter(target = Date.class)
	public static abstract class DateConverter {
		public final JsonReader.ReadObject<Date> JSON_READER = new JsonReader.ReadObject<Date>() {
			public Date read(JsonReader reader) throws IOException {
				return new Date(NumberConverter.deserializeLong(reader));
			}
		};
		public static final JsonWriter.WriteObject<Date> JSON_WRITER = new JsonWriter.WriteObject<Date>() {
			public void write(JsonWriter writer, @Nullable Date value) {
			}
		};
	}

	@JsonConverter(target = Short.class)
	public static abstract class ShortConverter {
		public static final JsonReader.ReadObject<Short> JSON_READER = new JsonReader.ReadObject<Short>() {
			public Short read(JsonReader reader) throws IOException {
				return 0;
			}
		};
		public static final JsonWriter.WriteObject<Integer> JSON_WRITER = new JsonWriter.WriteObject<Integer>() {
			public void write(JsonWriter writer, @Nullable Integer value) {
			}
		};
	}

	@JsonConverter(target = short.class)
	public static abstract class ShortPrimitiveConverter {
		public static short read(JsonReader<?> reader) throws IOException {
			return 0;
		}

		public static void write(JsonReader writer, Short value) {
		}
	}
}
