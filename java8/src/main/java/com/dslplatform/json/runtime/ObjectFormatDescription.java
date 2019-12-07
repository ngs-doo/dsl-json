package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.io.IOException;
import java.lang.reflect.*;

public final class ObjectFormatDescription<B, T> extends WriteDescription<T> implements FormatConverter<T>, JsonReader.BindObject<B> {

	private final Type manifest;
	private final InstanceFactory<B> newInstance;
	private final Settings.Function<B, T> finalize;
	private final DecodePropertyInfo<JsonReader.BindObject>[] decoders;
	private final boolean skipOnUnknown;
	private final boolean hasMandatory;
	private final long mandatoryFlag;
	private final String startError;
	private final String endError;

	private static final Settings.Function identity = new Settings.Function() {
		@Override
		public Object apply(@Nullable Object t) {
			return t;
		}
	};

	public static <D> ObjectFormatDescription<D, D> create(
			final Class<D> manifest,
			final InstanceFactory<D> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.BindObject>[] decoders,
			final DslJson json,
			final boolean skipOnUnknown) {
		return new ObjectFormatDescription<>(manifest, newInstance, identity, encoders, decoders, json, skipOnUnknown);
	}

	public ObjectFormatDescription(
			final Type manifest,
			final InstanceFactory<B> newInstance,
			final Settings.Function<B, T> finalize,
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
		this.startError = String.format("Expecting '{' to start decoding %s", Reflection.typeDescription(manifest));
		this.endError = String.format("Expecting '}' or ',' while decoding %s", Reflection.typeDescription(manifest));
	}

	@Nullable
	@Override
	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		else if (reader.last() != '{') {
			throw reader.newParseError(startError);
		}
		reader.getNextToken();
		return readContent(reader);
	}

	@Override
	public B bind(final JsonReader reader, final B instance) throws IOException {
		if (reader.last() != '{') {
			throw reader.newParseError(startError);
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
				throw reader.newParseErrorWith("Null value found for non-null attribute", ri.name);
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
				throw reader.newParseErrorWith("Null value found for non-null attribute", ri.name);
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
					throw reader.newParseErrorWith("Null value found for non-null attribute", ri.name);
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
			} else throw reader.newParseError(endError);
		}
		if (hasMandatory && currentMandatory != 0) {
			DecodePropertyInfo.showMandatoryError(reader, currentMandatory, decoders);
		}
	}

	private void skip(final JsonReader reader) throws IOException {
		if (!skipOnUnknown) {
			final String name = reader.getLastName();
			throw reader.newParseErrorFormat("Unknown property detected", name.length() + 3, "Unknown property detected: '%s' while reading %s", name, Reflection.typeDescription(manifest));
		}
		reader.getNextToken();
		reader.skip();
	}
}