package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonWriter;

abstract class WriteDescription<T> implements JsonWriter.WriteObject<T> {

	private final JsonWriter.WriteObject[] encoders;

	WriteDescription(final JsonWriter.WriteObject[] encoders) {
		if (encoders == null) throw new IllegalArgumentException("encoders can't be null");
		this.encoders = encoders.clone();
	}

	public final void write(final JsonWriter writer, final T instance) {
		if (instance == null) {
			writer.writeNull();
		} else {
			writer.writeByte(JsonWriter.OBJECT_START);
			writeObject(writer, instance);
			writer.writeByte(JsonWriter.OBJECT_END);
		}
	}

	public final void writeObject(final JsonWriter writer, final T instance) {
		if (encoders.length > 0) {
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
}
