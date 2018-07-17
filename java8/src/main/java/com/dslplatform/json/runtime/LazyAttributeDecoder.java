package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.lang.reflect.Type;

class LazyAttributeDecoder<T, P> implements JsonReader.BindObject<T> {

	private final Settings.BiConsumer<T, P> write;
	private JsonReader.ReadObject<P> decoder;
	private final DslJson json;
	private final Type type;

	LazyAttributeDecoder(
			final Settings.BiConsumer<T, P> write,
			final DslJson json,
			final Type type) {
		if (write == null) throw new IllegalArgumentException("write can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		if (type == null) throw new IllegalArgumentException("type can't be null");
		this.write = write;
		this.json = json;
		this.type = type;
	}

	@Override
	public T bind(final JsonReader reader, final T instance) throws IOException {
		if (decoder == null) {
			decoder = json.tryFindReader(type);
			if (decoder == null) {
				throw new SerializationException("Unable to find reader for " + type);
			}
		}
		final P attr = decoder.read(reader);
		write.accept(instance, attr);
		return instance;
	}
}
