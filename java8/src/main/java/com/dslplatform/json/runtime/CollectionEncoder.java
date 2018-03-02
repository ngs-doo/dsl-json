package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.util.Collection;

public final class CollectionEncoder<E, T extends Collection<E>> implements JsonWriter.WriteObject<T> {

	private final DslJson json;
	private final JsonWriter.WriteObject<E> encoder;

	public CollectionEncoder(
			final DslJson json,
			final JsonWriter.WriteObject<E> encoder) {
		if (json == null) throw new IllegalArgumentException("json can't be null");
		this.json = json;
		this.encoder = encoder;
	}

	private static final byte[] EMPTY = {'[', ']'};

	@Override
	public void write(final JsonWriter writer, final T value) {
		if (value == null) writer.writeNull();
		else if (value.isEmpty()) writer.writeAscii(EMPTY);
		else if (encoder != null) {
			boolean pastFirst = false;
			writer.writeByte(JsonWriter.ARRAY_START);
			for (final E e : value) {
				if (pastFirst) {
					writer.writeByte(JsonWriter.COMMA);
				} else {
					pastFirst = true;
				}
				encoder.write(writer, e);
			}
			writer.writeByte(JsonWriter.ARRAY_END);
		} else {
			boolean pastFirst = false;
			writer.writeByte(JsonWriter.ARRAY_START);
			Class<?> lastClass = null;
			JsonWriter.WriteObject lastEncoder = null;
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
						lastEncoder = json.tryFindWriter(lastClass);
						if (lastEncoder == null) {
							throw new SerializationException("Unable to find writer for " + lastClass);
						}
					}
					lastEncoder.write(writer, e);
				}
			}
			writer.writeByte(JsonWriter.ARRAY_END);
		}
	}
}
