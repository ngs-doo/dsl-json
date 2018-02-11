package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;

import java.io.IOException;
import java.util.Optional;

public final class OptionalDecoder<T> implements JsonReader.ReadObject<Optional<T>> {

	private final JsonReader.ReadObject<T> optReader;

	public OptionalDecoder(final JsonReader.ReadObject<T> reader) {
		if (reader == null) throw new IllegalArgumentException("reader can't be null");
		this.optReader = reader;
	}

	@Override
	public Optional<T> read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return Optional.empty();
		return Optional.ofNullable(optReader.read(reader));
	}
}
