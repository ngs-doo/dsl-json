package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.util.Map;

public final class EnumDescription<T extends Enum<T>> implements JsonWriter.WriteObject<T>, JsonReader.ReadObject<T> {

	private final Class<T> manifest;
	private final ReadPropertyInfo<T>[] readers;

	public EnumDescription(
			final Class<T> manifest,
			final Map<String, T> values) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (values == null) throw new IllegalArgumentException("values can't be null");
		this.manifest = manifest;
		final ReadPropertyInfo<T>[] tmp = new ReadPropertyInfo[values.size()];
		int i = 0;
		for (String name : values.keySet()) {
			tmp[i++] = new ReadPropertyInfo<>(name, false, values.get(name));
		}
		this.readers = ReadPropertyInfo.prepareReaders(tmp);
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
		for (final ReadPropertyInfo<T> ri : readers) {
			if (hash == ri.hash) {
				if (ri.exactName && !reader.wasLastName(ri.name)) continue;
				return ri.value;
			}
		}
		return Enum.valueOf(manifest, reader.getLastName());
	}
}
