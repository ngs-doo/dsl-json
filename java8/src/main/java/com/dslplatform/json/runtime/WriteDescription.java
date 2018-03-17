package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonWriter;

abstract class WriteDescription<T> implements JsonWriter.WriteObject<T> {

	private final boolean alwaysSerialize;
	private final boolean isEmpty;
	private final JsonWriter.WriteObject[] encoders;

	WriteDescription(final JsonWriter.WriteObject[] encoders, final boolean alwaysSerialize) {
		if (encoders == null) throw new IllegalArgumentException("encoders can't be null or empty");
		this.encoders = encoders.clone();
		this.alwaysSerialize = alwaysSerialize;
		this.isEmpty = encoders.length == 0;
	}

	public final void write(final JsonWriter writer, final T instance) {
		if (instance == null) {
			writer.writeNull();
		} else if (alwaysSerialize) {
			writer.writeByte(JsonWriter.OBJECT_START);
			writeContentFull(writer, instance);
			writer.writeByte(JsonWriter.OBJECT_END);
		} else {
			writer.writeByte(JsonWriter.OBJECT_START);
			writeContentMinimal(writer, instance);
			writer.writeByte(JsonWriter.OBJECT_END);
		}
	}

	public final void writeContentFull(final JsonWriter writer, final T instance) {
		if (isEmpty) return;
		encoders[0].write(writer, instance);
		for (int i = 1; i < encoders.length; i++) {
			writer.writeByte(JsonWriter.COMMA);
			encoders[i].write(writer, instance);
		}
	}

	public final boolean writeContentMinimal(final JsonWriter writer, final T instance) {
		if (isEmpty) return false;
		final int originalPos = writer.size();
		final long originalFlushed = writer.flushed();
		int pos = originalPos;
		long flushed = originalFlushed;
		encoders[0].write(writer, instance);
		if (writer.size() != pos || writer.flushed() != flushed) {
			writer.writeByte(JsonWriter.COMMA);
			pos = writer.size();
			flushed = writer.flushed();
		}
		for (int i = 1; i < encoders.length; i++) {
			encoders[i].write(writer, instance);
			if (writer.size() != pos || writer.flushed() != flushed) {
				writer.writeByte(JsonWriter.COMMA);
				pos = writer.size();
				flushed = writer.flushed();
			}
		}
		return originalPos != pos || originalFlushed != flushed;
	}
}
