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
	private final boolean skipOnUnknown;
	private final boolean hasMandatory;
	private final long mandatoryFlag;

	public ImmutableDescription(
			final Class<T> manifest,
			final Object[] defArgs,
			final Function<Object[], T> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.ReadObject>[] decoders,
			final boolean skipOnUnknown) {
		this((Type) manifest, defArgs, newInstance, encoders, decoders, skipOnUnknown);
	}

	ImmutableDescription(
			final Type manifest,
			final Object[] defArgs,
			final Function<Object[], T> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.ReadObject>[] decoders,
			final boolean skipOnUnknown) {
		super(encoders);
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (defArgs == null) throw new IllegalArgumentException("defArgs can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (decoders == null) throw new IllegalArgumentException("decoders can't be null");
		this.manifest = manifest;
		this.defArgs = defArgs;
		this.newInstance = newInstance;
		this.decoders = DecodePropertyInfo.prepare(decoders);
		this.skipOnUnknown = skipOnUnknown;
		this.mandatoryFlag = DecodePropertyInfo.calculateMandatory(this.decoders);
		hasMandatory = mandatoryFlag != 0;
	}

	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return newInstance.apply(null);
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + " while parsing " + manifest + ". Found " + (char) reader.last());
		}
		if (reader.getNextToken() == '}') {
			if (hasMandatory) {
				DecodePropertyInfo.showMandatoryError(reader, mandatoryFlag, decoders);
			}
			return newInstance.apply(defArgs);
		}
		final Object[] args = defArgs.clone();
		long currentMandatory = mandatoryFlag;
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
					args[ri.index] = ri.value.read(reader);
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
			throw new IOException("Expecting '}' at position " + reader.positionInStream() + " while parsing " + manifest + ". Found " + (char) reader.last());
		}
		if (hasMandatory && currentMandatory != 0) {
			DecodePropertyInfo.showMandatoryError(reader, currentMandatory, decoders);
		}
		return newInstance.apply(args);
	}
}
