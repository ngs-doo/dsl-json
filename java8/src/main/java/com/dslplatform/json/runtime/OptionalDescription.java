package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.util.Optional;

public final class OptionalDescription<T> implements JsonWriter.WriteObject<Optional<T>>, JsonReader.ReadObject<Optional<T>> {

	private final DslJson json;
	private final JsonWriter.WriteObject<T> optWriter;
	private final JsonReader.ReadObject<T> optReader;

	public OptionalDescription(
			final DslJson json,
			final JsonWriter.WriteObject<T> writer,
			final JsonReader.ReadObject<T> reader) {
		if (writer == null) throw new IllegalArgumentException("writer can't be null");
		if (reader == null) throw new IllegalArgumentException("reader can't be null");
		this.json = json;
		this.optWriter = writer;
		this.optReader = reader;
	}

	@Override
	public void write(JsonWriter writer, Optional<T> value) {
		if (value == null || !value.isPresent()) writer.writeNull();
		else if (json != null) {
			final T unpacked = value.get();
			if (unpacked == null) writer.writeNull();
			else {
				final JsonWriter.WriteObject jw = json.tryFindWriter(unpacked.getClass());
				if (jw == null) throw new SerializationException("Unable to find writer for " + unpacked.getClass());
				jw.write(writer, unpacked);
			}
		} else optWriter.write(writer, value.get());
	}

	@Override
	public Optional<T> read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return Optional.empty();
		return Optional.ofNullable(optReader.read(reader));
	}
}
