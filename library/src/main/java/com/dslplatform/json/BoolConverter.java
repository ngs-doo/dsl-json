package com.dslplatform.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public abstract class BoolConverter {

	public final static boolean[] EMPTY_ARRAY = new boolean[0];

	public static final JsonReader.ReadObject<Boolean> READER = reader -> deserialize(reader);
	public static final JsonReader.ReadObject<Boolean> NULLABLE_READER = new JsonReader.ReadObject<Boolean>() {
		@Nullable
		@Override
		public Boolean read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserialize(reader);
		}
	};
	public static final JsonWriter.WriteObject<Boolean> WRITER = (writer, value) -> serializeNullable(value, writer);
	public static final JsonReader.ReadObject<boolean[]> ARRAY_READER = new JsonReader.ReadObject<boolean[]>() {
		@Nullable
		@Override
		public boolean[] read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			if (reader.last() != '[') throw reader.newParseError("Expecting '[' for boolean array start");
			reader.getNextToken();
			return deserializeBoolArray(reader);
		}
	};
	public static final JsonWriter.WriteObject<boolean[]> ARRAY_WRITER = (writer, value) -> serialize(value, writer);

	static <T> void registerDefault(DslJson<T> json) {
		json.registerReader(boolean.class, READER);
		json.registerWriter(boolean.class, WRITER);
		json.registerDefault(boolean.class, false);
		json.registerReader(boolean[].class, ARRAY_READER);
		json.registerWriter(boolean[].class, ARRAY_WRITER);
		json.registerReader(Boolean.class, NULLABLE_READER);
		json.registerWriter(Boolean.class, WRITER);
	}

	public static void serializeNullable(@Nullable final Boolean value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value) {
			sw.writeAscii("true");
		} else {
			sw.writeAscii("false");
		}
	}

	public static void serialize(final boolean value, final JsonWriter sw) {
		if (value) {
			sw.writeAscii("true");
		} else {
			sw.writeAscii("false");
		}
	}

	public static void serialize(@Nullable final boolean[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			sw.writeAscii(value[0] ? "true" : "false");
			for(int i = 1; i < value.length; i++) {
				sw.writeAscii(value[i] ? ",true" : ",false");
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	public static boolean deserialize(final JsonReader reader) throws IOException {
		if (reader.wasTrue()) {
			return true;
		} else if (reader.wasFalse()) {
			return false;
		}
		throw reader.newParseErrorAt("Found invalid boolean value", 0);
	}

	public static boolean[] deserializeBoolArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return EMPTY_ARRAY;
		}
		boolean[] buffer = new boolean[4];
		buffer[0] = deserialize(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserialize(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Boolean> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(READER);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<Boolean> res) throws IOException {
		reader.deserializeCollection(READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Boolean> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(READER);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<Boolean> res) throws IOException {
		reader.deserializeNullableCollection(READER, res);
	}
}
