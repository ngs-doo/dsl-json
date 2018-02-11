package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.SerializationException;

import java.util.Map;

public final class MapEncoder<K, V, T extends Map<K, V>> implements JsonWriter.WriteObject<T> {

	private final DslJson json;
	private final JsonWriter.WriteObject<K> keyWriter;
	private final JsonWriter.WriteObject<V> valueWriter;

	public MapEncoder(
			final DslJson json,
			final JsonWriter.WriteObject<K> keyWriter,
			final JsonWriter.WriteObject<V> valueWriter) {
		if (json == null) throw new IllegalArgumentException("json can't be null");
		this.json = json;
		this.keyWriter = keyWriter;
		this.valueWriter = valueWriter;
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
}
