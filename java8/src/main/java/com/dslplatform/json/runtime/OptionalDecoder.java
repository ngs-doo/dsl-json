package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;

import java.io.IOException;
import java.util.Optional;

public final class OptionalDecoder<T> implements JsonReader.ReadObject<Optional<T>> {

	private final JsonReader.ReadObject<T> decoder;

	public OptionalDecoder(final JsonReader.ReadObject<T> decoder) {
		if (decoder == null) throw new IllegalArgumentException("decoder can't be null");
		this.decoder = decoder;
	}

	@Override
	public Optional<T> read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return Optional.empty();
		return Optional.ofNullable(decoder.read(reader));
	}
}
