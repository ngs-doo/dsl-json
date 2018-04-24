package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Function;

public final class ArrayFormatDescription<B, T> implements FormatConverter<T>, JsonReader.BindObject<B> {

	private final Type manifest;
	private final InstanceFactory<B> newInstance;
	private final Function<B, T> finalize;
	private final boolean isEmpty;
	private final JsonWriter.WriteObject[] encoders;
	private final JsonReader.BindObject[] decoders;

	private static final Function identity = new Function() {
		@Override
		public Object apply(Object t) {
			return t;
		}
	};

	public static <D> ArrayFormatDescription<D, D> create(
			final Class<D> manifest,
			final InstanceFactory<D> newInstance,
			final JsonWriter.WriteObject[] encoders,
			final JsonReader.BindObject[] decoders) {
		return new ArrayFormatDescription<>(manifest, newInstance, identity, encoders, decoders);
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
		if (encoders.length != decoders.length) throw new IllegalArgumentException("decoders must match encoders (" + decoders.length + " != " + encoders.length + ")");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.finalize = finalize;
		this.isEmpty = encoders.length == 0;
		this.encoders = encoders.clone();
		this.decoders = decoders.clone();
	}

	@Override
	public final void write(final JsonWriter writer, final T instance) {
		if (instance == null) {
			writer.writeNull();
		} else {
			writer.writeByte(JsonWriter.ARRAY_START);
			writeContentFull(writer, instance);
			writer.writeByte(JsonWriter.ARRAY_END);
		}
	}

	@Override
	public void writeContentFull(final JsonWriter writer, final T instance) {
		if (isEmpty) return;
		encoders[0].write(writer, instance);
		for (int i = 1; i < encoders.length; i++) {
			writer.writeByte(JsonWriter.COMMA);
			encoders[i].write(writer, instance);
		}
	}

	@Override
	public boolean writeContentMinimal(final JsonWriter writer, final T instance) {
		writeContentFull(writer, instance);
		return false;
	}

	@Override
	public T read(final JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		final B instance = newInstance.create();
		bind(reader, instance);
		return finalize.apply(instance);
	}

	@Override
	public B bind(final JsonReader reader, final B instance) throws IOException {
		if (reader.last() != '[') {
			throw new IOException("Expecting '[' " + reader.positionDescription() + " while decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
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
		int i = 0;
		while (i < decoders.length) {
			decoders[i].bind(reader, instance);
			i++;
			if (reader.getNextToken() == ',') reader.getNextToken();
			else break;
		}
		if (i != decoders.length) {
			throw new IOException("Expecting to read " + decoders.length + " elements in the array while decoding " + manifest.getTypeName() + ". Read only: " +  i + " at position: " + reader.positionInStream());
		}
		if (reader.last() != ']') {
			throw new IOException("Expecting ']' " + reader.positionDescription() + " while decoding " + manifest.getTypeName() + ". Found " + (char) reader.last());
		}
	}
}