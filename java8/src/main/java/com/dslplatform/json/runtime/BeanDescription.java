package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.concurrent.Callable;

public final class BeanDescription<T> extends WriteDescription<T> implements JsonReader.ReadObject<T>, JsonReader.BindObject<T> {

	public final Type manifest;
	final Callable<T> newInstance;
	private final DecodePropertyInfo<JsonReader.BindObject>[] decoders;
	private final boolean skipOnUnknown;
	private final boolean hasMandatory;
	private final long mandatoryFlag;

	public BeanDescription(
			final Class<T> manifest,
			final Callable<T> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.BindObject>[] decoders,
			final boolean skipOnUnknown) {
		this((Type) manifest, newInstance, encoders, decoders, skipOnUnknown);
	}

	BeanDescription(
			final Type manifest,
			final Callable<T> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.BindObject>[] decoders,
			final boolean skipOnUnknown) {
		super(encoders);
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (decoders == null) throw new IllegalArgumentException("decoders can't be null");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.decoders = DecodePropertyInfo.prepare(decoders);
		this.skipOnUnknown = skipOnUnknown;
		this.mandatoryFlag = DecodePropertyInfo.calculateMandatory(this.decoders);
		hasMandatory = mandatoryFlag != 0;
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
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
		}
		if (reader.getNextToken() == '}') {
			if (hasMandatory) {
				DecodePropertyInfo.showMandatoryError(reader, mandatoryFlag, decoders);
			}
			return instance;
		}
		long currentMandatory = mandatoryFlag;
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
					currentMandatory = currentMandatory & ri.mandatoryValue;
					processed = true;
					break;
				}
			}
			if (!processed) {
				if (!skipOnUnknown) {
					final String name = reader.getLastName();
					throw new IOException("Unknown property detected: " + name + " at position " + reader.positionInStream(name.length() + 3));
				}
				reader.skip();
			}
			if (reader.getNextToken() != ',') break;
		} while (reader.getNextToken() == '"');
		if (reader.last() != '}') {
			throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
		}
		if (hasMandatory && currentMandatory != 0) {
			DecodePropertyInfo.showMandatoryError(reader, currentMandatory, decoders);
		}
		return instance;
	}
}