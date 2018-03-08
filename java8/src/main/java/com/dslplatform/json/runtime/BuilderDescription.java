package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.util.function.Function;

public final class BuilderDescription<B, T> extends WriteDescription<T> implements JsonReader.ReadObject<T> {

	private final JsonReader.ReadObject<B> decoder;
	private final Function<B, T> newInstance;

	public BuilderDescription(
			final JsonReader.ReadObject<B> decoder,
			final Function<B, T> newInstance,
			final JsonWriter.WriteObject[] encoders) {
		super(encoders);
		if (decoder == null) throw new IllegalArgumentException("decoder can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		this.decoder = decoder;
		this.newInstance = newInstance;
	}

	public T read(final JsonReader reader) throws IOException {
		final B builder = decoder.read(reader);
		return newInstance.apply(builder);
	}
}
