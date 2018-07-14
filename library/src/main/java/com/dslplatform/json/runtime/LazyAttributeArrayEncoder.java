package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.lang.reflect.Type;

class LazyAttributeArrayEncoder<T, R> implements JsonWriter.WriteObject<T> {

	private final Function<T, R> read;
	private JsonWriter.WriteObject<R> encoder;
	private final DslJson json;
	private final Type type;

	LazyAttributeArrayEncoder(
			final Function<T, R> read,
			final DslJson json,
			final Type type) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		this.read = read;
		this.json = json;
		this.type = type;
	}

	@Override
	public void write(final JsonWriter writer, final T value) {
		if (type != null && encoder == null) {
			encoder = json.tryFindWriter(type);
			if (encoder == null) {
				throw new SerializationException("Unable to find writer for " + type);
			}
		}
		final R attr = read.apply(value);
		if (attr == null) {
			writer.writeNull();
		} else if (type == null) {
			final JsonWriter.WriteObject tmp = json.tryFindWriter(attr.getClass());
			if (tmp == null) {
				throw new SerializationException("Unable to find writer for " + attr.getClass());
			}
			tmp.write(writer, attr);
		} else {
			encoder.write(writer, attr);
		}
	}
}
