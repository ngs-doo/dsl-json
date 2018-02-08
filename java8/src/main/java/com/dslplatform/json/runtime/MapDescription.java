package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.Callable;

public final class MapDescription<K, V, T extends Map<K, V>> implements JsonWriter.WriteObject<T>, JsonReader.ReadObject<T> {

	private final Type manifest;
	private final Callable<T> newInstance;
	private final DslJson json;
	private final JsonWriter.WriteObject<K> keyWriter;
	private final JsonReader.ReadObject<K> keyReader;
	private final JsonWriter.WriteObject<V> valueWriter;
	private final JsonReader.ReadObject<V> valueReader;

	public MapDescription(
			final Type manifest,
			final Callable<T> newInstance,
			final DslJson json,
			final JsonWriter.WriteObject<K> keyWriter,
			final JsonReader.ReadObject<K> keyReader,
			final JsonWriter.WriteObject<V> valueWriter,
			final JsonReader.ReadObject<V> valueReader) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		if (keyReader == null) throw new IllegalArgumentException("keyReader can't be null");
		if (valueReader == null) throw new IllegalArgumentException("valueReader can't be null");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.json = json;
		this.keyWriter = keyWriter;
		this.keyReader = keyReader;
		this.valueWriter = valueWriter;
		this.valueReader = valueReader;
	}

	private static final byte[] EMPTY = {'{', '}'};

	@Override
	public void write(JsonWriter writer, T value) {
		if (value == null) writer.writeNull();
		else if (value.isEmpty()) writer.writeAscii(EMPTY);
		else if (keyWriter != null && valueWriter != null) {
			boolean pastFirst = false;
			writer.writeByte(JsonWriter.OBJECT_START);
			for (final Map.Entry<K, V> e : value.entrySet()) {
				if (pastFirst) {
					writer.writeByte(JsonWriter.COMMA);
				} else {
					pastFirst = true;
				}
				keyWriter.write(writer, e.getKey());
				writer.writeByte(JsonWriter.SEMI);
				valueWriter.write(writer, e.getValue());
			}
			writer.writeByte(JsonWriter.OBJECT_END);
		} else {
			boolean pastFirst = false;
			writer.writeByte(JsonWriter.OBJECT_START);
			Class<?> lastKeyClass = null;
			Class<?> lastValueClass = null;
			JsonWriter.WriteObject lastKeyWriter = keyWriter;
			JsonWriter.WriteObject lastValueWriter = null;
			for (final Map.Entry<K, V> e : value.entrySet()) {
				if (pastFirst) {
					writer.writeByte(JsonWriter.COMMA);
				} else {
					pastFirst = true;
				}
				final Class<?> currentKeyClass = e.getKey().getClass();
				if (keyWriter == null && currentKeyClass != lastKeyClass) {
					lastKeyClass = currentKeyClass;
					lastKeyWriter = json.tryFindWriter(lastKeyClass);
					if (lastKeyWriter == null)
						throw new SerializationException("Unable to find writer for " + lastKeyClass);
				}
				lastKeyWriter.write(writer, e.getKey());
				writer.writeByte(JsonWriter.SEMI);
				if (valueWriter != null) {
					valueWriter.write(writer, e.getValue());
				} else {
					if (e.getValue() == null) {
						writer.writeNull();
					} else {
						final Class<?> currentValueClass = e.getValue().getClass();
						if (currentValueClass != lastValueClass) {
							lastValueClass = currentValueClass;
							lastValueWriter = json.tryFindWriter(lastValueClass);
							if (lastValueWriter == null)
								throw new SerializationException("Unable to find writer for " + lastValueClass);
						}
						lastValueWriter.write(writer, e.getValue());
					}
				}
			}
			writer.writeByte(JsonWriter.OBJECT_END);
		}
	}

	@Override
	public T read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		final T instance;
		try {
			instance = newInstance.call();
		} catch (Exception e) {
			throw new IOException("Unable to create a new instance of " + manifest, e);
		}
		if (reader.getNextToken() == '}') return instance;
		K key = keyReader.read(reader);
		if (key == null) {
			throw new IOException("Null value detected for key element of " + manifest + " at position " + reader.positionInStream());
		}
		if (reader.getNextToken() != ':') {
			throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		reader.getNextToken();
		V value = valueReader.read(reader);
		instance.put(key, value);
		while (reader.getNextToken() == ','){
			reader.getNextToken();
			key = keyReader.read(reader);
			if (key == null) {
				throw new IOException("Null value detected for key element of " + manifest + " at position " + reader.positionInStream());
			}
			if (reader.getNextToken() != ':') {
				throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
			}
			reader.getNextToken();
			value = valueReader.read(reader);
			instance.put(key, value);
		}
		if (reader.last() != '}') {
			throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		return instance;
	}
}
