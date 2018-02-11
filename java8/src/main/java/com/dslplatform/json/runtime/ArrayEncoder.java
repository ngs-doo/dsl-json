package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

public final class ArrayEncoder<T> implements JsonWriter.WriteObject<T[]> {

	private final DslJson json;
	private final JsonWriter.WriteObject<T> elementWriter;

	public ArrayEncoder(
			final DslJson json,
			final JsonWriter.WriteObject<T> writer) {
		if (json == null) throw new IllegalArgumentException("json can't be null");
		this.json = json;
		this.elementWriter = writer;
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
}
