package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.Nullable;

class AttributeArrayEncoder<T, R> implements JsonWriter.WriteObject<T> {

	private final Settings.Function<T, R> read;
	private final JsonWriter.WriteObject<R> encoder;

	AttributeArrayEncoder(
			final Settings.Function<T, R> read,
			final JsonWriter.WriteObject<R> encoder) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (encoder == null) throw new IllegalArgumentException("encoder can't be null");
		this.read = read;
		this.encoder = encoder;
	}

	@Override
	public void write(final JsonWriter writer, @Nullable final T value) {
		final R attr = read.apply(value);
		encoder.write(writer, attr);
	}
}
