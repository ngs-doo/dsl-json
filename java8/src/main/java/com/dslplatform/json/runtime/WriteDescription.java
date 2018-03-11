package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonWriter;

abstract class WriteDescription<T> implements JsonWriter.WriteObject<T> {

	private final boolean alwaysSerialize;
	final boolean isEmpty;
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
		} else if (isEmpty) {
			writer.writeByte(JsonWriter.OBJECT_START);
			writer.writeByte(JsonWriter.OBJECT_END);
		} else if (alwaysSerialize) {
			writer.writeByte(JsonWriter.OBJECT_START);
			writeObjectFull(writer, instance);
			writer.writeByte(JsonWriter.OBJECT_END);
		} else {
			writer.writeByte(JsonWriter.OBJECT_START);
			writeObjectMinimal(writer, instance);
			writer.writeByte(JsonWriter.OBJECT_END);
		}
	}

	final void writeObjectFull(final JsonWriter writer, final T instance) {
		encoders[0].write(writer, instance);
		for (int i = 1; i < encoders.length; i++) {
			writer.writeByte(JsonWriter.COMMA);
			encoders[i].write(writer, instance);
		}
	}

	final void writeObjectMinimal(final JsonWriter writer, final T instance) {
		int pos = writer.size();
		long flushed = writer.flushed();
		encoders[0].write(writer, instance);
		for (int i = 1; i < encoders.length; i++) {
			if (writer.size() != pos || writer.flushed() != flushed) {
				writer.writeByte(JsonWriter.COMMA);
				pos = writer.size();
				flushed = writer.flushed();
			}
			encoders[i].write(writer, instance);
		}
	}
}
