package com.dslplatform.json.runtime;

import com.dslplatform.json.ConfigurationException;
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
		if (reader.last() != '{') throw reader.newParseError("Expecting '{' for map start");
		final T instance;
		try {
			instance = newInstance.call();
		} catch (Exception e) {
			throw new ConfigurationException("Unable to create a new instance of " + Reflection.typeDescription(manifest), e);
		}
		if (reader.getNextToken() == '}') return instance;
		K key = keyDecoder.read(reader);
		if (key == null) {
			throw reader.newParseErrorFormat("Null value detected for key element of map", 0, "Null value detected for key element of %s", Reflection.typeDescription(manifest));
		}
		if (reader.getNextToken() != ':') throw reader.newParseError("Expecting ':' after key attribute");
		reader.getNextToken();
		V value = valueDecoder.read(reader);
		instance.put(key, value);
		while (reader.getNextToken() == ','){
			reader.getNextToken();
			key = keyDecoder.read(reader);
			if (key == null) throw reader.newParseErrorFormat("Null value detected for key element of map", 0, "Null value detected for key element of %s", Reflection.typeDescription(manifest));
			if (reader.getNextToken() != ':') throw reader.newParseError("Expecting ':' after key attribute");
			reader.getNextToken();
			value = valueDecoder.read(reader);
			instance.put(key, value);
		}
		if (reader.last() != '}') throw reader.newParseError("Expecting '}' as map ending");
		return instance;
	}
}
