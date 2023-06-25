package com.dslplatform.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class BinaryConverter {

	public static final JsonReader.ReadObject<byte[]> READER = new JsonReader.ReadObject<byte[]>() {
		@Nullable
		@Override
		public byte[] read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserialize(reader);
		}
	};

	public static final JsonWriter.WriteObject<byte[]> WRITER = (writer, value) -> serialize(value, writer);

	public static final byte[] EMPTY_ARRAY = new byte[0];

	static <T> void registerDefault(DslJson<T> json) {
		json.registerReader(byte[].class, READER);
		json.registerWriter(byte[].class, (writer, value) -> serialize(value, writer));
	}

	public static void serialize(@Nullable final byte[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("\"\"");
		} else {
			sw.writeBinary(value);
		}
	}

	public static byte[] deserialize(final JsonReader reader) throws IOException {
		return reader.readBase64();
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<byte[]> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(READER);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<byte[]> res) throws IOException {
		reader.deserializeCollection(READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<byte[]> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(READER);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<byte[]> res) throws IOException {
		reader.deserializeNullableCollection(READER, res);
	}
}
