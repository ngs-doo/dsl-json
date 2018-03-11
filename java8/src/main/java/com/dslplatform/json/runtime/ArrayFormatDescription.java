package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Function;

public final class ArrayFormatDescription<B, T> implements JsonWriter.WriteObject<T>, JsonReader.ReadObject<T>, JsonReader.BindObject<B> {

	private final Type manifest;
	final InstanceFactory<B> newInstance;
	final Function<B, T> finalize;
	final boolean isEmpty;
	private final JsonWriter.WriteObject[] encoders;
	private final JsonReader.BindObject[] decoders;

	public static <D> ArrayFormatDescription<D, D> create(
			final Class<D> manifest,
			final InstanceFactory<D> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final JsonReader.BindObject[] decoders) {
		return new ArrayFormatDescription<>(manifest, newInstance, t -> t, encoders, decoders);
	}

	public ArrayFormatDescription(
			final Type manifest,
			final InstanceFactory<B> newInstance,
			final Function<B, T> finalize,
			final JsonWriter.WriteObject[] encoders,
			final JsonReader.BindObject[] decoders) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("create can't be null");
		if (finalize == null) throw new IllegalArgumentException("finalize can't be null");
		if (encoders == null) throw new IllegalArgumentException("encoders can't be null or empty");
		if (decoders == null) throw new IllegalArgumentException("decoders can't be null");
		if (encoders.length != decoders.length) throw new IllegalArgumentException("decoders must match encoders");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.finalize = finalize;
		this.isEmpty = encoders.length == 0;
		this.encoders = encoders.clone();
		this.decoders = decoders.clone();
	}

	public final void write(final JsonWriter writer, final T instance) {
		if (instance == null) {
			writer.writeNull();
		} else if (isEmpty) {
			writer.writeByte(JsonWriter.ARRAY_START);
			writer.writeByte(JsonWriter.ARRAY_END);
		} else {
			writer.writeByte(JsonWriter.ARRAY_START);
			writeContent(writer, instance);
			writer.writeByte(JsonWriter.ARRAY_END);
		}
	}

	final void writeContent(final JsonWriter writer, final T instance) {
		encoders[0].write(writer, instance);
		for (int i = 1; i < encoders.length; i++) {
			writer.writeByte(JsonWriter.COMMA);
			encoders[i].write(writer, instance);
		}
	}

	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		final B instance = newInstance.create();
		bind(reader, instance);
		return finalize.apply(instance);
	}

	public B bind(final JsonReader reader, final B instance) throws IOException {
		if (reader.last() != '[') {
			throw new IOException("Expecting '[' at position " + reader.positionInStream() + " while decoding " + manifest + ". Found " + (char) reader.last());
		}
		reader.getNextToken();
		return bindObject(reader, instance);
	}

	public B bindObject(final JsonReader reader, final B instance) throws IOException {
		int i;
		for (i = 0; i < decoders.length; i++) {
			reader.getNextToken();
			decoders[i].bind(reader, instance);
			if (reader.getNextToken() == ',') reader.getNextToken();
			else break;
		}
		if (i != decoders.length) {
			throw new IOException("Expecting to read " + decoders.length + " elements in the array while decoding " + manifest + ". Read only: " +  i + " at position " + reader.positionInStream());
		}
		if (reader.last() != ']') {
			throw new IOException("Expecting ']' at position " + reader.positionInStream() + " while decoding " + manifest + ". Found " + (char) reader.last());
		}
		return instance;
	}
}