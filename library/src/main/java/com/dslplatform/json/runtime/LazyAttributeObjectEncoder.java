package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class LazyAttributeObjectEncoder<T, R> implements JsonWriter.WriteObject<T> {

	private final Settings.Function<T, R> read;
	private final byte[] quotedName;
	private final boolean alwaysSerialize;
	private JsonWriter.WriteObject<R> encoder;
	private final Object defaultValue;
	private final DslJson json;
	private final Type type;

	LazyAttributeObjectEncoder(
			final Settings.Function<T, R> read,
			final String name,
			final DslJson json,
			@Nullable final Type type) {
		if (read == null) throw new IllegalArgumentException("read can't be null");
		if (name == null || name.isEmpty()) throw new IllegalArgumentException("name can't be null");
		if (json == null) throw new IllegalArgumentException("json can't be null");
		this.read = read;
		quotedName = ("\"" + name + "\":").getBytes(StandardCharsets.UTF_8);
		this.alwaysSerialize = !json.omitDefaults();
		this.json = json;
		this.type = type;
		this.defaultValue = json.getDefault(type);
	}

	@Override
	public void write(final JsonWriter writer, @Nullable final T value) {
		if (type != null && encoder == null) {
			encoder = json.tryFindWriter(type);
			if (encoder == null) {
				throw new ConfigurationException("Unable to find writer for " + type);
			}
		}
		final R attr = read.apply(value);
		if (type == null) {
			if (attr == null) {
				if (alwaysSerialize) {
					writer.writeAscii(quotedName);
					writer.writeNull();
				}
			} else {
				final Class<?> manifest = attr.getClass();
				final JsonWriter.WriteObject tmp = json.tryFindWriter(manifest);
				if (tmp == null) {
					throw new ConfigurationException("Unable to find writer for " + manifest);
				}
				if (!alwaysSerialize) {
					final Object tmpDefault = json.getDefault(manifest);
					if (attr == tmpDefault) return;
				}
				writer.writeAscii(quotedName);
				tmp.write(writer, attr);
			}
		} else if (alwaysSerialize || attr != defaultValue) {
			writer.writeAscii(quotedName);
			encoder.write(writer, attr);
		}
	}
}
