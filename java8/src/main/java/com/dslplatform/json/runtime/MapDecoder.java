package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.Callable;

public final class MapDecoder<K, V, T extends Map<K, V>> implements JsonReader.ReadObject<T> {

	private final Type manifest;
	private final Callable<T> newInstance;
	private final JsonReader.ReadObject<K> keyDecoder;
	private final JsonReader.ReadObject<V> valueDecoder;

	public MapDecoder(
			final Type manifest,
			final Callable<T> newInstance,
			final JsonReader.ReadObject<K> keyDecoder,
			final JsonReader.ReadObject<V> valueDecoder) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("create can't be null");
		if (keyDecoder == null) throw new IllegalArgumentException("keyDecoder can't be null");
		if (valueDecoder == null) throw new IllegalArgumentException("valueDecoder can't be null");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.keyDecoder = keyDecoder;
		this.valueDecoder = valueDecoder;
	}

	@Nullable
	@Override
	public T read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' " + reader.positionDescription() + ". Found " + (char)reader.last());
		}
		final T instance;
		try {
			instance = newInstance.call();
		} catch (Exception e) {
			throw new IOException("Unable to create a new instance of " + manifest.getTypeName(), e);
		}
		if (reader.getNextToken() == '}') return instance;
		K key = keyDecoder.read(reader);
		if (key == null) {
			throw new IOException("Null value detected for key element of " + manifest.getTypeName() + " " + reader.positionDescription());
		}
		if (reader.getNextToken() != ':') {
			throw new IOException("Expecting ':' " + reader.positionDescription() + ". Found " + (char)reader.last());
		}
		reader.getNextToken();
		V value = valueDecoder.read(reader);
		instance.put(key, value);
		while (reader.getNextToken() == ','){
			reader.getNextToken();
			key = keyDecoder.read(reader);
			if (key == null) {
				throw new IOException("Null value detected for key element of " + manifest.getTypeName() + " " + reader.positionDescription());
			}
			if (reader.getNextToken() != ':') {
				throw new IOException("Expecting ':' " + reader.positionDescription() + ". Found " + (char)reader.last());
			}
			reader.getNextToken();
			value = valueDecoder.read(reader);
			instance.put(key, value);
		}
		if (reader.last() != '}') {
			throw new IOException("Expecting '}' " + reader.positionDescription() + ". Found " + (char)reader.last());
		}
		return instance;
	}
}
