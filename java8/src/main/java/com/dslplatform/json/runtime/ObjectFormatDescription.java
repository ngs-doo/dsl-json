package com.dslplatform.json.runtime;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.function.Function;

public final class ObjectFormatDescription<B, T> extends WriteDescription<T> implements FormatConverter<T>, JsonReader.BindObject<B> {

	private final Type manifest;
	private final InstanceFactory<B> newInstance;
	private final Function<B, T> finalize;
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

	@Override
	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		else if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position: " + reader.positionInStream() + " while reading " + manifest.getTypeName() + ". Found " + (char) reader.last());
		}
		reader.getNextToken();
		return readContent(reader);
	}

	@Override
	public B bind(final JsonReader reader, final B instance) throws IOException {
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position: " + reader.positionInStream() + " while binding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		}
		reader.getNextToken();
		bindContent(reader, instance);
		return instance;
	}

	@Override
	public T readContent(final JsonReader reader) throws IOException {
		final B instance = newInstance.create();
		bindContent(reader, instance);
		return finalize.apply(instance);
	}

	private void bindContent(final JsonReader reader, final B instance) throws IOException {
		if (reader.last() == '}') {
			if (hasMandatory) {
				DecodePropertyInfo.showMandatoryError(reader, mandatoryFlag, decoders);
			}
			return;
		}
		long currentMandatory = mandatoryFlag;
		int i = 0;
		while(i < decoders.length) {
			final DecodePropertyInfo<JsonReader.BindObject> ri = decoders[i++];
			final int weakHash = reader.fillNameWeakHash();
			if (weakHash != ri.weakHash || !reader.wasLastName(ri.nameBytes)) {
				bindObjectSlow(reader, instance, currentMandatory);
				return;
			}
			reader.getNextToken();
			if (ri.nonNull && reader.wasNull()) {
				throw new IOException("Null value found for property " + ri.name + " at position: " + reader.positionInStream());
			}
			ri.value.bind(reader, instance);
			currentMandatory = currentMandatory & ri.mandatoryValue;
			if (reader.getNextToken() == ',' && i != decoders.length) reader.getNextToken();
			else break;
		}
		finalChecks(reader, instance, currentMandatory);
	}

	private void bindObjectSlow(final JsonReader reader, final B instance, long currentMandatory) throws IOException {
		boolean processed = false;
		final int oldHash = reader.getLastHash();
		for (final DecodePropertyInfo<JsonReader.BindObject> ri : decoders) {
			if (oldHash != ri.hash) continue;
			if (ri.exactName) {
				if (!reader.wasLastName(ri.nameBytes)) continue;
			}
			reader.getNextToken();
			if (ri.nonNull && reader.wasNull()) {
				throw new IOException("Null value found for property " + ri.name + " at position: " + reader.positionInStream());
			}
			ri.value.bind(reader, instance);
			currentMandatory = currentMandatory & ri.mandatoryValue;
			processed = true;
			break;
		}
		if (!processed) skip(reader);
		else reader.getNextToken();
		while (reader.last() == ','){
			reader.getNextToken();
			final int hash = reader.fillName();
			processed = false;
			for (final DecodePropertyInfo<JsonReader.BindObject> ri : decoders) {
				if (hash != ri.hash) continue;
				if (ri.exactName) {
					if (!reader.wasLastName(ri.nameBytes)) continue;
				}
				reader.getNextToken();
				if (ri.nonNull && reader.wasNull()) {
					throw new IOException("Null value found for property " + ri.name + " at position: " + reader.positionInStream());
				}
				ri.value.bind(reader, instance);
				currentMandatory = currentMandatory & ri.mandatoryValue;
				processed = true;
				break;
			}
			if (!processed) skip(reader);
			else reader.getNextToken();
		}
		finalChecks(reader, instance, currentMandatory);
	}

	private void finalChecks(final JsonReader reader, final B instance, final long currentMandatory) throws IOException {
		if (reader.last() != '}') {
			if (reader.last() == ',') {
				reader.getNextToken();
				reader.fillNameWeakHash();
				bindObjectSlow(reader, instance, currentMandatory);
				return;
			} else throw new IOException("Expecting '}' or ',' at position: " + reader.positionInStream() + " while reading " + manifest.getTypeName() + ". Found " + (char) reader.last());
		}
		if (hasMandatory && currentMandatory != 0) {
			DecodePropertyInfo.showMandatoryError(reader, currentMandatory, decoders);
		}
	}

	private void skip(final JsonReader reader) throws IOException {
		if (!skipOnUnknown) {
			final String name = reader.getLastName();
			throw new IOException("Unknown property detected: '" + name + "' while reading " + manifest.getTypeName() + " at position: " + reader.positionInStream(name.length() + 3));
		}
		reader.getNextToken();
		reader.skip();
	}
}