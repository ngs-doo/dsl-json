package com.dslplatform.json;

import java.io.IOException;
import java.util.*;

public abstract class ObjectConverter {

	private static final JsonReader.ReadObject<Map<String, Object>> TypedMapReader = new JsonReader.ReadObject<Map<String, Object>>() {
		@Override
		public Map<String, Object> read(JsonReader reader) throws IOException {
			return deserializeMap(reader);
		}
	};
	private static final JsonReader.ReadObject<Object> ObjectReader = new JsonReader.ReadObject<Object>() {
		@Override
		public Object read(JsonReader reader) throws IOException {
			return deserializeObject(reader);
		}
	};
	@SuppressWarnings("rawtypes")
	static final JsonReader.ReadObject<Collection> CollectionReader = new JsonReader.ReadObject<Collection>() {
		@Override
		public Collection read(JsonReader reader) throws IOException {
			return deserializeList(reader);
		}
	};
	@SuppressWarnings("rawtypes")
	static final JsonReader.ReadObject<LinkedHashMap> MapReader = new JsonReader.ReadObject<LinkedHashMap>() {
		@Override
		public LinkedHashMap read(JsonReader reader) throws IOException {
			return deserializeMap(reader);
		}
	};

	public static void serializeNullableMap(final Map<String, Object> value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serializeMap(value, sw);
		}
	}

	public static void serializeMap(final Map<String, Object> value, final JsonWriter sw) {
		sw.writeByte(JsonWriter.OBJECT_START);
		final int size = value.size();
		if (size > 0) {
			final Iterator<Map.Entry<String, Object>> iterator = value.entrySet().iterator();
			Map.Entry<String, Object> kv = iterator.next();
			sw.writeString(kv.getKey());
			sw.writeByte(JsonWriter.SEMI);
			sw.serializeObject(kv.getValue());
			for (int i = 1; i < size; i++) {
				sw.writeByte(JsonWriter.COMMA);
				kv = iterator.next();
				sw.writeString(kv.getKey());
				sw.writeByte(JsonWriter.SEMI);
				sw.serializeObject(kv.getValue());
			}
		}
		sw.writeByte(JsonWriter.OBJECT_END);
	}

	//TODO: not a nice hack. fixme
	private static DslJson<Object> SHARED;

	public static void serializeObject(final Object value, final JsonWriter sw) throws IOException {
		if (SHARED == null) {
			SHARED = new DslJson<Object>();
		}
		SHARED.serialize(sw, value);
	}

	public static Object deserializeObject(final JsonReader reader) throws IOException {
		switch (reader.last()) {
			case 'n':
				if (!reader.wasNull()) {
					throw reader.expecting("null");
				}
				return null;
			case 't':
				if (!reader.wasTrue()) {
					throw reader.expecting("true");
				}
				return true;
			case 'f':
				if (!reader.wasFalse()) {
					throw reader.expecting("false");
				}
				return false;
			case '"':
				return reader.readString();
			case '{':
				return deserializeMap(reader);
			case '[':
				return deserializeList(reader);
			default:
				return NumberConverter.deserializeNumber(reader);
		}
	}

	public static ArrayList<Object> deserializeList(final JsonReader reader) throws IOException {
		if (reader.last() != '[') {
			throw reader.expecting("[");
		}
		byte nextToken = reader.getNextToken();
		if (nextToken == ']') return new ArrayList<Object>(0);
		final ArrayList<Object> res = new ArrayList<Object>(4);
		res.add(deserializeObject(reader));
		while ((nextToken = reader.getNextToken()) == ',') {
			reader.getNextToken();
			res.add(deserializeObject(reader));
		}
		if (nextToken != ']') {
			throw reader.expecting("]", nextToken);
		}
		return res;
	}

	public static LinkedHashMap<String, Object> deserializeMap(final JsonReader reader) throws IOException {
		if (reader.last() != '{') {
			throw reader.expecting("{");
		}
		byte nextToken = reader.getNextToken();
		if (nextToken == '}') return new LinkedHashMap<String, Object>(0);
		final LinkedHashMap<String, Object> res = new LinkedHashMap<String, Object>();
		String key = reader.readKey();
		res.put(key, deserializeObject(reader));
		while ((nextToken = reader.getNextToken()) == ',') {
			reader.getNextToken();
			key = reader.readKey();
			res.put(key, deserializeObject(reader));
		}
		if (nextToken != '}') {
			throw reader.expecting("}", nextToken);
		}
		return res;
	}

	public static ArrayList<Map<String, Object>> deserializeMapCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(TypedMapReader);
	}

	public static void deserializeMapCollection(final JsonReader reader, final Collection<Map<String, Object>> res) throws IOException {
		reader.deserializeCollection(TypedMapReader, res);
	}

	public static ArrayList<Map<String, Object>> deserializeNullableMapCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(TypedMapReader);
	}

	public static void deserializeNullableMapCollection(final JsonReader reader, final Collection<Map<String, Object>> res) throws IOException {
		reader.deserializeNullableCollection(TypedMapReader, res);
	}
}
