package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;

import java.io.IOException;
import java.util.ArrayList;

public final class ArrayDecoder<T> implements JsonReader.ReadObject<T[]> {

	private final T[] emptyInstance;
	private final JsonReader.ReadObject<T> elementReader;

	public ArrayDecoder(
			final T[] emptyInstance,
			final JsonReader.ReadObject<T> reader) {
		if (emptyInstance == null) throw new IllegalArgumentException("emptyInstance can't be null");
		if (reader == null) throw new IllegalArgumentException("reader can't be null");
		this.emptyInstance = emptyInstance;
		this.elementReader = reader;
	}

	@Override
	public T[] read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() != '[') {
			throw new IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		if (reader.getNextToken() == ']') return emptyInstance;
		final ArrayList<T> list = new ArrayList<>(4);
		list.add(elementReader.read(reader));
		while (reader.getNextToken() == ','){
			reader.getNextToken();
			list.add(elementReader.read(reader));
		}
		if (reader.last() != ']') {
			throw new IOException("Expecting ']' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		return list.toArray(emptyInstance);
	}
}
