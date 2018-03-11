package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.function.Function;

public final class BeanDescription<B, T> extends WriteDescription<T> implements JsonReader.ReadObject<T>, JsonReader.BindObject<B> {

	private static final Charset utf8 = Charset.forName("UTF-8");

	final Type manifest;
	final Callable<B> newInstance;
	final Function<B, T> finalize;
	private final DecodePropertyInfo<JsonReader.BindObject>[] decoders;
	private final boolean skipOnUnknown;
	private final boolean hasMandatory;
	private final long mandatoryFlag;
	final int typeHash;
	final byte[] typeName;

	public static <D> BeanDescription<D, D> create(
			final Class<D> manifest,
			final Callable<D> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.BindObject>[] decoders,
			final boolean skipOnUnknown) {
		return new BeanDescription<>(manifest, newInstance, t -> t, encoders, decoders, manifest.getTypeName(), skipOnUnknown);
	}

	public BeanDescription(
			final Type manifest,
			final Callable<B> newInstance,
			final Function<B, T> finalize,
			final JsonWriter.WriteObject[] encoders,
			final DecodePropertyInfo<JsonReader.BindObject>[] decoders,
			final String typeName,
			final boolean skipOnUnknown) {
		super(encoders);
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (decoders == null) throw new IllegalArgumentException("decoders can't be null");
		if (typeName == null) throw new IllegalArgumentException("typeName can't be null");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.finalize = finalize;
		this.decoders = DecodePropertyInfo.prepare(decoders);
		this.skipOnUnknown = skipOnUnknown;
		this.mandatoryFlag = DecodePropertyInfo.calculateMandatory(this.decoders);
		this.hasMandatory = mandatoryFlag != 0;
		String name = typeName.replace("$", ".");
		this.typeName = ("\"" + name + "\"").getBytes(utf8);
		this.typeHash = DecodePropertyInfo.calcHash(name);
	}

	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		final B instance;
		try {
			instance = newInstance.call();
		} catch (Exception e) {
			throw new IOException("Unable to create an instance of " + manifest, e);
		}
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