package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;

import java.io.IOException;
import java.util.ArrayList;

public final class ArrayDecoder<T> implements JsonReader.ReadObject<T[]> {

	private final T[] emptyInstance;
	private final JsonReader.ReadObject<T> decoder;

	public ArrayDecoder(
			final T[] emptyInstance,
			final JsonReader.ReadObject<T> decoder) {
		if (emptyInstance == null) throw new IllegalArgumentException("emptyInstance can't be null");
		if (decoder == null) throw new IllegalArgumentException("decoder can't be null");
		this.emptyInstance = emptyInstance;
		this.decoder = decoder;
	}

	@Override
	public T[] read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() != '[') {
			throw new IOException("Expecting '[' " + reader.positionDescription() + ". Found " + (char)reader.last());
		}
		if (reader.getNextToken() == ']') return emptyInstance;
		final ArrayList<T> list = new ArrayList<T>(4);
		list.add(decoder.read(reader));
		while (reader.getNextToken() == ','){
			reader.getNextToken();
			list.add(decoder.read(reader));
		}
		if (reader.last() != ']') {
			throw new IOException("Expecting ']' " + reader.positionDescription() + ". Found " + (char)reader.last());
		}
		return list.toArray(emptyInstance);
	}
}
