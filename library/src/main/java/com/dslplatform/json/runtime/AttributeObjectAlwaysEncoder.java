package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class AttributeObjectAlwaysEncoder<T, R> implements JsonWriter.WriteObject<T> {

	private final Settings.Function<T, R> read;
	private final byte[] quotedName;
	private final JsonWriter.WriteObject<R> encoder;

	AttributeObjectAlwaysEncoder(
			final Settings.Function<T, R> read,
			final String name,
			final JsonWriter.WriteObject<R> encoder) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (name == null || name.isEmpty()) throw new IllegalArgumentException("name can't be null");
		if (encoder == null) throw new IllegalArgumentException("encoder can't be null");
		this.read = read;
		quotedName = ("\"" + name + "\":").getBytes(StandardCharsets.UTF_8);
		this.encoder = encoder;
	}

	@Override
	public void write(final JsonWriter writer, @Nullable final T value) {
		final R attr = read.apply(value);
		writer.writeAscii(quotedName);
		encoder.write(writer, attr);
	}
}
