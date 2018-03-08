package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;

public final class EnumDescription<T extends Enum<T>> implements JsonWriter.WriteObject<T>, JsonReader.ReadObject<T> {

	private final Class<T> manifest;
	private final DecodePropertyInfo<T>[] decoders;

	public EnumDescription(
			final Class<T> manifest,
			final T[] values) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (values == null) throw new IllegalArgumentException("values can't be null");
		this.manifest = manifest;
		final DecodePropertyInfo<T>[] tmp = new DecodePropertyInfo[values.length];
		for (int i = 0; i < values.length; i++) {
			T value = values[i];
			tmp[i] = new DecodePropertyInfo<>(value.name(), false, false, i, value);
		}
		this.decoders = DecodePropertyInfo.prepare(tmp);
	}

	@Override
	public void write(final JsonWriter writer, final T value) {
		if (value == null) writer.writeNull();
		else writer.writeString(value.name());
	}

	@Override
	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		final int hash = reader.calcHash();
		for (final DecodePropertyInfo<T> ri : decoders) {
			if (hash == ri.hash) {
				if (ri.exactName && !reader.wasLastName(ri.name)) continue;
				return ri.value;
			}
		}
		return Enum.valueOf(manifest, reader.getLastName());
	}
}
