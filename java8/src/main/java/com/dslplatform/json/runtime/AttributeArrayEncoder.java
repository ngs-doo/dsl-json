package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonWriter;

import java.util.function.Function;

public class AttributeArrayEncoder<T, R> implements JsonWriter.WriteObject<T> {

	private final Function<T, R> read;
	private final JsonWriter.WriteObject<R> encoder;

	public AttributeArrayEncoder(
			final Function<T, R> read,
			final JsonWriter.WriteObject<R> encoder) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (encoder == null) throw new IllegalArgumentException("encoder can't be null");
		this.read = read;
		this.encoder = encoder;
	}

	@Override
	public void write(final JsonWriter writer, final T value) {
		final R attr = read.apply(value);
		encoder.write(writer, attr);
	}
}
