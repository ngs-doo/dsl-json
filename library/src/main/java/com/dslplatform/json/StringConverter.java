package com.dslplatform.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class StringConverter {

	static final JsonReader.ReadObject<String> Reader = new JsonReader.ReadObject<String>() {
		@Override
		public String read(JsonReader reader) throws IOException {
			return reader.readString();
		}
	};
	static final JsonWriter.WriteObject<String> Writer = new JsonWriter.WriteObject<String>() {
		@Override
		public void write(JsonWriter writer, String value) {
			serializeNullable(value, writer);
		}
	};

	public static void serializeShortNullable(final String value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			sw.writeString(value);
		}
	}

	public static void serializeShort(final String value, final JsonWriter sw) {
		sw.writeString(value);
	}

	public static void serializeNullable(final String value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			sw.writeString(value);
		}
	}

	public static void serialize(final String value, final JsonWriter sw) {
		sw.writeString(value);
	}

	public static String deserialize(final JsonReader reader) throws IOException {
		return reader.readString();
	}

	public static String deserializeNullable(final JsonReader reader) throws IOException {
		if (reader.last() == 'n') {
			if (!reader.wasNull()) throw new IOException("Expecting 'null' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
			return null;
		}
		return reader.readString();
	}

	public static ArrayList<String> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(Reader);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<String> res) throws IOException {
		reader.deserializeCollection(Reader, res);
	}

	public static ArrayList<String> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(Reader);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<String> res) throws IOException {
		reader.deserializeNullableCollection(Reader, res);
	}

	public static void serialize(final List<String> list, final JsonWriter writer) {
		writer.writeByte(JsonWriter.ARRAY_START);
		if (list.size() != 0) {
			writer.writeString(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				writer.writeByte(JsonWriter.COMMA);
				writer.writeString(list.get(i));
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}
}
