package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.Callable;

public final class CollectionDescription<E, T extends Collection<E>> implements JsonWriter.WriteObject<T>, JsonReader.ReadObject<T> {

	private final Type manifest;
	private final Callable<T> newInstance;
	private final DslJson json;
	private final JsonWriter.WriteObject<E> elementWriter;
	private final JsonReader.ReadObject<E> elementReader;

	public CollectionDescription(
			final Type manifest,
			final Callable<T> newInstance,
			final DslJson json,
			final JsonWriter.WriteObject<E> writer,
			final JsonReader.ReadObject<E> reader) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		if (reader == null) throw new IllegalArgumentException("reader can't be null");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.json = json;
		this.elementWriter = writer;
		this.elementReader = reader;
	}

	private static final byte[] EMPTY = {'[', ']'};

	@Override
	public void write(JsonWriter writer, T value) {
		if (value == null) writer.writeNull();
		else if (value.isEmpty()) writer.writeAscii(EMPTY);
		else if (elementWriter != null) {
			boolean pastFirst = false;
			writer.writeByte(JsonWriter.ARRAY_START);
			for (final E e : value) {
				if (pastFirst) {
					writer.writeByte(JsonWriter.COMMA);
				} else {
					pastFirst = true;
				}
				elementWriter.write(writer, e);
			}
			writer.writeByte(JsonWriter.ARRAY_END);
		} else {
			boolean pastFirst = false;
			writer.writeByte(JsonWriter.ARRAY_START);
			Class<?> lastClass = null;
			JsonWriter.WriteObject lastWriter = null;
			for (final E e : value) {
				if (pastFirst) {
					writer.writeByte(JsonWriter.COMMA);
				} else {
					pastFirst = true;
				}
				if (e == null) writer.writeNull();
				else {
					final Class<?> currentClass = e.getClass();
					if (currentClass != lastClass) {
						lastClass = currentClass;
						lastWriter = json.tryFindWriter(lastClass);
						if (lastWriter == null) {
							throw new SerializationException("Unable to find writer for " + lastClass);
						}
					}
					lastWriter.write(writer, e);
				}
			}
			writer.writeByte(JsonWriter.ARRAY_END);
		}
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
