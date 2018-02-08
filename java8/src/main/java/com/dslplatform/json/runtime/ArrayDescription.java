package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.util.ArrayList;

public final class ArrayDescription<T> implements JsonWriter.WriteObject<T[]>, JsonReader.ReadObject<T[]> {

	private final T[] emptyInstance;
	private final DslJson json;
	private final JsonWriter.WriteObject<T> elementWriter;
	private final JsonReader.ReadObject<T> elementReader;

	public ArrayDescription(
			final T[] emptyInstance,
			final DslJson json,
			final JsonWriter.WriteObject<T> writer,
			final JsonReader.ReadObject<T> reader) {
		if (emptyInstance == null) throw new IllegalArgumentException("emptyInstance can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		if (reader == null) throw new IllegalArgumentException("reader can't be null");
		this.emptyInstance = emptyInstance;
		this.json = json;
		this.elementWriter = writer;
		this.elementReader = reader;
	}

	private static final byte[] EMPTY = {'[', ']'};

	@Override
	public void write(JsonWriter writer, T[] value) {
		if (value == null) writer.writeNull();
		else if (value.length == 0) writer.writeAscii(EMPTY);
		else if (elementWriter != null) {
			writer.writeByte(JsonWriter.ARRAY_START);
			elementWriter.write(writer, value[0]);
			for (int i = 1; i < value.length; i++) {
				writer.writeByte(JsonWriter.COMMA);
				elementWriter.write(writer, value[i]);
			}
			writer.writeByte(JsonWriter.ARRAY_END);
		} else {
			boolean pastFirst = false;
			writer.writeByte(JsonWriter.ARRAY_START);
			Class<?> lastClass = null;
			JsonWriter.WriteObject lastWriter = null;
			for (final T e : value) {
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
	public T[] read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() != '[') {
			throw new IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		if (reader.getNextToken() == ']') return emptyInstance;
		final ArrayList<T> list = new ArrayList<>(4);
		list.add(elementReader.read(reader));
		while (reader.getNextToken() == ','){
			reader.getNextToken();
			list.add(elementReader.read(reader));
		}
		if (reader.last() != ']') {
			throw new IOException("Expecting ']' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		return list.toArray(emptyInstance);
	}
}
