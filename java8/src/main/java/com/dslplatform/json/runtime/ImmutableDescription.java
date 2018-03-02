package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Function;

public final class ImmutableDescription<T> extends WriteDescription<T> implements JsonReader.ReadObject<T> {

	private final Type manifest;
	private final Object[] defArgs;
	private final Function<Object[], T> newInstance;
	private final DecodePropertyInfo<JsonReader.ReadObject>[] decoders;

	public ImmutableDescription(
			final Type manifest,
			final Object[] defArgs,
			final Function<Object[], T> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.ReadObject>[] decoders) {
		super(encoders);
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (defArgs == null) throw new IllegalArgumentException("defArgs can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (decoders == null) throw new IllegalArgumentException("decoders can't be null");
		this.manifest = manifest;
		this.defArgs = defArgs;
		this.newInstance = newInstance;
		this.decoders = DecodePropertyInfo.prepare(decoders);
	}

	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return newInstance.apply(null);
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + " while parsing " + manifest + ". Found " + (char) reader.last());
		}
		if (reader.getNextToken() == '}') return newInstance.apply(defArgs);
		final Object[] args = defArgs.clone();
		do {
			final int hash = reader.fillName();
			boolean processed = false;
			for (int i = 0; i < decoders.length; i++) {
				final DecodePropertyInfo<JsonReader.ReadObject> ri = decoders[i];
				if (hash == ri.hash) {
					if (ri.exactName) {
						if (!reader.wasLastName(ri.name)) continue;
					}
					reader.getNextToken();
					args[i] = ri.value.read(reader);
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
