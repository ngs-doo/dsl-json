package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Function;

final class ImmutableDescription<T> extends WriteDescription<T> implements JsonReader.ReadObject<T> {

	private final Type manifest;
	private final Object[] defArgs;
	private final Function<Object[], T> newInstance;
	private final ReadPropertyInfo<JsonReader.ReadObject>[] readers;

	ImmutableDescription(
			final Type manifest,
			final Object[] defArgs,
			final Function<Object[], T> newInstance,
			final JsonWriter.WriteObject[] writers,
			final ReadPropertyInfo<JsonReader.ReadObject>[] readers) {
		super(writers);
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (defArgs == null) throw new IllegalArgumentException("defArgs can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (readers == null) throw new IllegalArgumentException("readers can't be null");
		this.manifest = manifest;
		this.defArgs = defArgs;
		this.newInstance = newInstance;
		this.readers = ReadPropertyInfo.prepareReaders(readers);
	}

	public T read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return newInstance.apply(null);
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + " while parsing " + manifest + ". Found " + (char) reader.last());
		}
		if (reader.getNextToken() == '}') return newInstance.apply(defArgs);
		final Object[] args = defArgs.clone();
		do {
			final int hash = reader.fillName();
			boolean processed = false;
			for (int i = 0; i < readers.length; i++) {
				final ReadPropertyInfo<JsonReader.ReadObject> ri = readers[i];
				if (hash == ri.hash) {
					if (ri.exactName) {
						if (!reader.wasLastName(ri.name)) continue;
					}
					reader.getNextToken();
					args[i] = ri.reader.read(reader);
					processed = true;
					break;
				}
			}
			if (!processed) {
				reader.skip();
			}
			if (reader.getNextToken() != ',') break;
		} while (reader.getNextToken() == '"');
		if (reader.last() != '}') {
			throw new IOException("Expecting '}' at position " + reader.positionInStream() + " while parsing " + manifest + ". Found " + (char) reader.last());
		}
		return newInstance.apply(args);
	}
}
