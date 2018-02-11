package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.Callable;

public final class CollectionEncoder<E, T extends Collection<E>> implements JsonWriter.WriteObject<T> {

	private final DslJson json;
	private final JsonWriter.WriteObject<E> elementWriter;

	public CollectionEncoder(
			final DslJson json,
			final JsonWriter.WriteObject<E> writer) {
		if (json == null) throw new IllegalArgumentException("json can't be null");
		this.json = json;
		this.elementWriter = writer;
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
}
