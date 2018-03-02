package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.concurrent.Callable;

public final class BeanDescription<T> extends WriteDescription<T> implements JsonReader.ReadObject<T>, JsonReader.BindObject<T> {

	public final Type manifest;
	private final Callable<T> newInstance;
	private final DecodePropertyInfo<JsonReader.BindObject>[] decoders;

	public BeanDescription(
			final Type manifest,
			final Callable<T> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.BindObject>[] decoders) {
		super(encoders);
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (decoders == null) throw new IllegalArgumentException("decoders can't be null");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.decoders = DecodePropertyInfo.prepare(decoders);
	}

	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		final T instance;
		try {
			instance = newInstance.call();
		} catch (Exception e) {
			throw new IOException("Unable to create an instance of " + manifest, e);
		}
		return bind(reader, instance);
	}

	public T bind(final JsonReader reader, final T instance) throws IOException {
		if (reader.last() != '{') {
			throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		if (reader.getNextToken() == '}') return instance;
		do {
			final int hash = reader.fillName();
			boolean processed = false;
			for (final DecodePropertyInfo<JsonReader.BindObject> ri : decoders) {
				if (hash == ri.hash) {
					if (ri.exactName) {
						if (!reader.wasLastName(ri.name)) continue;
					}
					reader.getNextToken();
					ri.value.bind(reader, instance);
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
			throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		return instance;
	}
}
