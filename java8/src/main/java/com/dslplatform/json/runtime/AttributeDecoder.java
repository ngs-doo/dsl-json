package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;

import java.io.IOException;
import java.util.function.BiConsumer;

public class AttributeDecoder<T, P> implements JsonReader.BindObject<T> {

	private final BiConsumer<T, P> write;
	private final JsonReader.ReadObject<P> decoder;

	public AttributeDecoder(
			final BiConsumer<T, P> write,
			final JsonReader.ReadObject<P> decoder) {
		if (write == null) throw new IllegalArgumentException("write can't be null");
		if (decoder == null) throw new IllegalArgumentException("decoder can't be null");
		this.write = write;
		this.decoder = decoder;
	}

	@Override
	public T bind(final JsonReader reader, final T instance) throws IOException {
		final P attr = decoder.read(reader);
		write.accept(instance, attr);
		return instance;
	}
}
