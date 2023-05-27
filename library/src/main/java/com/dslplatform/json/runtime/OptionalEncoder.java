package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.util.Optional;

public final class OptionalEncoder<T> implements JsonWriter.WriteObject<Optional<T>> {

	private final DslJson json;
	private final JsonWriter.WriteObject<T> encoder;

	public OptionalEncoder(
			final DslJson json,
			@Nullable final JsonWriter.WriteObject<T> encoder) {
		if (json == null) throw new IllegalArgumentException("json can't be null");
		this.json = json;
		this.encoder = encoder;
	}

	@Override
	public void write(JsonWriter writer, @Nullable Optional<T> value) {
		if (value == null || !value.isPresent()) writer.writeNull();
		else if (encoder != null) encoder.write(writer, value.get());
		else {
			final T unpacked = value.get();
			final JsonWriter.WriteObject jw = json.tryFindWriter(unpacked.getClass());
			if (jw == null) {
				throw new ConfigurationException("Unable to find writer for " + unpacked.getClass());
			}
			jw.write(writer, unpacked);
		}
	}
}
