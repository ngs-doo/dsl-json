package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import com.dslplatform.json.SerializationException;

import java.util.Map;

public final class MapEncoder<K, V, T extends Map<K, V>> implements JsonWriter.WriteObject<T> {

	private final DslJson json;
	private final boolean checkForConversionToString;
	private final JsonWriter.WriteObject<K> keyWriter;
	private final JsonWriter.WriteObject<V> valueWriter;

	public MapEncoder(
			final DslJson json,
			final boolean checkForConversionToString,
			final JsonWriter.WriteObject<K> keyWriter,
			final JsonWriter.WriteObject<V> valueWriter) {
		if (json == null) throw new IllegalArgumentException("json can't be null");
		this.json = json;
		this.checkForConversionToString = checkForConversionToString;
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
				if (checkForConversionToString) {
					writeQuoted(writer, keyWriter, e.getKey());
				} else keyWriter.write(writer, e.getKey());
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
					if (lastKeyWriter == null) {
						throw new SerializationException("Unable to find writer for " + lastKeyClass);
					}
				}
				writeQuoted(writer, lastKeyWriter, e.getKey());
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

	private void writeQuoted(final JsonWriter writer, final JsonWriter.WriteObject<K> keyWriter, final K key) {
		if (key instanceof Double) {
			final double value = (Double) key;
			if (value == Double.NaN) writer.writeAscii("NaN");
			else if (value == Double.POSITIVE_INFINITY) writer.writeAscii("Infinity");
			else if (value == Double.NEGATIVE_INFINITY) writer.writeAscii("-Infinity");
			else {
				writer.writeByte(JsonWriter.QUOTE);
				NumberConverter.serialize(value, writer);
				writer.writeByte(JsonWriter.QUOTE);
			}
		} else if (key instanceof Float) {
			final float value = (Float) key;
			if (value == Float.NaN) writer.writeAscii("NaN");
			else if (value == Float.POSITIVE_INFINITY) writer.writeAscii("Infinity");
			else if (value == Float.NEGATIVE_INFINITY) writer.writeAscii("-Infinity");
			else {
				writer.writeByte(JsonWriter.QUOTE);
				NumberConverter.serialize(value, writer);
				writer.writeByte(JsonWriter.QUOTE);
			}
		} else if (key instanceof Number) {
			writer.writeByte(JsonWriter.QUOTE);
			keyWriter.write(writer, key);
			writer.writeByte(JsonWriter.QUOTE);
		} else {
			keyWriter.write(writer, key);
		}
	}
}
