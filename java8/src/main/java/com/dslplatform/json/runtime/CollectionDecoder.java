package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.Callable;

public final class CollectionDecoder<E, T extends Collection<E>> implements JsonReader.ReadObject<T> {

	private final Type manifest;
	private final Callable<T> newInstance;
	private final JsonReader.ReadObject<E> elementReader;

	public CollectionDecoder(
			final Type manifest,
			final Callable<T> newInstance,
			final JsonReader.ReadObject<E> reader) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (reader == null) throw new IllegalArgumentException("reader can't be null");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.elementReader = reader;
	}

	@Override
	public T read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() != '[') {
			throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		final T instance;
		try {
			instance = newInstance.call();
		} catch (Exception e) {
			throw new IOException("Unable to create a new instance of " + manifest, e);
		}
		if (reader.getNextToken() == ']') return instance;
		instance.add(elementReader.read(reader));
		while (reader.getNextToken() == ','){
			reader.getNextToken();
			instance.add(elementReader.read(reader));
		}
		if (reader.last() != ']') {
			throw new java.io.IOException("Expecting ']' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		return instance;
	}
}
