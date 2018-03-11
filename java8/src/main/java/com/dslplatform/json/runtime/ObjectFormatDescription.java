package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.function.Function;

public final class ObjectFormatDescription<B, T> extends WriteDescription<T> implements JsonReader.ReadObject<T>, JsonReader.BindObject<B> {

	final Type manifest;
	final InstanceFactory<B> newInstance;
	final Function<B, T> finalize;
	private final DecodePropertyInfo<JsonReader.BindObject>[] decoders;
	private final boolean skipOnUnknown;
	private final boolean hasMandatory;
	private final long mandatoryFlag;

	public static <D> ObjectFormatDescription<D, D> create(
			final Class<D> manifest,
			final InstanceFactory<D> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.BindObject>[] decoders,
			final DslJson json,
			final boolean skipOnUnknown) {
		return new ObjectFormatDescription<>(manifest, newInstance, t -> t, encoders, decoders, json, skipOnUnknown);
	}

	public ObjectFormatDescription(
			final Type manifest,
			final InstanceFactory<B> newInstance,
			final Function<B, T> finalize,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.BindObject>[] decoders,
			final DslJson json,
			final boolean skipOnUnknown) {
		super(encoders, !json.omitDefaults);
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("create can't be null");
		if (finalize == null) throw new IllegalArgumentException("finalize can't be null");
		if (decoders == null) throw new IllegalArgumentException("decoders can't be null");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.finalize = finalize;
		this.decoders = DecodePropertyInfo.prepare(decoders);
		this.skipOnUnknown = skipOnUnknown;
		this.mandatoryFlag = DecodePropertyInfo.calculateMandatory(this.decoders);
		this.hasMandatory = mandatoryFlag != 0;
	}

	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		final B instance = newInstance.create();
		bind(reader, instance);
		return finalize.apply(instance);
	}

	public B bind(final JsonReader reader, final B instance) throws IOException {
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
		}
		reader.getNextToken();
		return bindObject(reader, instance);
	}

	public B bindObject(final JsonReader reader, final B instance) throws IOException {
		if (reader.last() == '}') {
			if (hasMandatory) {
				DecodePropertyInfo.showMandatoryError(reader, mandatoryFlag, decoders);
			}
			return instance;
		}
		long currentMandatory = mandatoryFlag;
		for (final DecodePropertyInfo<JsonReader.BindObject> ri : decoders) {
			final int weakHash = reader.fillNameWeakHash();
			if (weakHash != ri.weakHash || !reader.wasLastName(ri.nameBytes)) {
				return bindObjectSlow(reader, instance, currentMandatory);
			}
			reader.getNextToken();
			ri.value.bind(reader, instance);
			currentMandatory = currentMandatory & ri.mandatoryValue;
			if (reader.getNextToken() == ',') reader.getNextToken();
			else break;
		}
		return checkAndReturn(reader, instance, currentMandatory);
	}

	private B bindObjectSlow(final JsonReader reader, final B instance, long currentMandatory) throws IOException {
		boolean processed = false;
		final int oldHash = reader.getLastHash();
		for (final DecodePropertyInfo<JsonReader.BindObject> ri : decoders) {
			if (oldHash != ri.hash) continue;
			if (ri.exactName) {
				if (!reader.wasLastName(ri.nameBytes)) continue;
			}
			reader.getNextToken();
			ri.value.bind(reader, instance);
			currentMandatory = currentMandatory & ri.mandatoryValue;
			processed = true;
			break;
		}
		if (!processed) skip(reader);
		while (reader.getNextToken() == ','){
			reader.getNextToken();
			final int hash = reader.fillName();
			processed = false;
			for (final DecodePropertyInfo<JsonReader.BindObject> ri : decoders) {
				if (hash != ri.hash) continue;
				if (ri.exactName) {
					if (!reader.wasLastName(ri.nameBytes)) continue;
				}
				reader.getNextToken();
				ri.value.bind(reader, instance);
				currentMandatory = currentMandatory & ri.mandatoryValue;
				processed = true;
				break;
			}
			if (!processed) skip(reader);
		}
		return checkAndReturn(reader, instance, currentMandatory);
	}

	private B checkAndReturn(final JsonReader reader, final B instance, final long currentMandatory) throws IOException {
		if (reader.last() != '}') {
			throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
		}
		if (hasMandatory && currentMandatory != 0) {
			DecodePropertyInfo.showMandatoryError(reader, currentMandatory, decoders);
		}
		return instance;
	}

	private void skip(final JsonReader reader) throws IOException {
		if (!skipOnUnknown) {
			final String name = reader.getLastName();
			throw new IOException("Unknown property detected: " + name + " at position " + reader.positionInStream(name.length() + 3));
		}
		reader.skip();
	}
}