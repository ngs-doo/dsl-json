package com.dslplatform.json;

import java.io.IOException;
import java.util.*;

public abstract class MapConverter {

	private static final JsonReader.ReadObject<Map<String, String>> TypedMapReader = new JsonReader.ReadObject<Map<String, String>>() {
		@Override
		public Map<String, String> read(JsonReader reader) throws IOException {
			return deserialize(reader);
		}
	};

	public static void serializeNullable(final Map<String, String> value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final Map<String, String> value, final JsonWriter sw) {
		sw.writeByte(JsonWriter.OBJECT_START);
		final int size = value.size();
		if (size > 0) {
			final Iterator<Map.Entry<String, String>> iterator = value.entrySet().iterator();
			Map.Entry<String, String> kv = iterator.next();
			StringConverter.serializeShort(kv.getKey(), sw);
			sw.writeByte(JsonWriter.SEMI);
			StringConverter.serializeNullable(kv.getValue(), sw);
			for (int i = 1; i < size; i++) {
				sw.writeByte(JsonWriter.COMMA);
				kv = iterator.next();
				StringConverter.serializeShort(kv.getKey(), sw);
				sw.writeByte(JsonWriter.SEMI);
				StringConverter.serializeNullable(kv.getValue(), sw);
			}
		}
		sw.writeByte(JsonWriter.OBJECT_END);
	}

	public static Map<String, String> deserialize(final JsonReader reader) throws IOException {
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
		}
		byte nextToken = reader.getNextToken();
		if (nextToken == '}') return new LinkedHashMap<String, String>(0);
		final LinkedHashMap<String, String> res = new LinkedHashMap<String, String>();
		String key = StringConverter.deserialize(reader);
		nextToken = reader.getNextToken();
		if (nextToken != ':') {
			throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
		}
		reader.getNextToken();
		String value = StringConverter.deserializeNullable(reader);
		res.put(key, value);
		while ((nextToken = reader.getNextToken()) == ',') {
			reader.getNextToken();
			key = StringConverter.deserialize(reader);
			nextToken = reader.getNextToken();
			if (nextToken != ':') {
				throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
			}
			reader.getNextToken();
			value = StringConverter.deserializeNullable(reader);
			res.put(key, value);
		}
		if (nextToken != '}') {
			throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
		}
		return res;
	}

	public static ArrayList<Map<String, String>> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(TypedMapReader);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<Map<String, String>> res) throws IOException {
		reader.deserializeCollection(TypedMapReader, res);
	}

	public static ArrayList<Map<String, String>> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(TypedMapReader);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<Map<String, String>> res) throws IOException {
		reader.deserializeNullableCollection(TypedMapReader, res);
	}
}
