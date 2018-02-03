package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonWriter;

abstract class WriteDescription<T> implements JsonWriter.WriteObject<T> {

	private final JsonWriter.WriteObject[] writers;

	WriteDescription(final JsonWriter.WriteObject[] writers) {
		if (writers == null) throw new IllegalArgumentException("writers can't be null");
		this.writers = writers.clone();
	}

	public final void write(final JsonWriter writer, final T instance) {
		if (instance == null) {
			writer.writeNull();
		} else {
			writer.writeByte(JsonWriter.OBJECT_START);
			int pos = writer.size();
			long flushed = writer.flushed();
			if (writers.length > 0) {
				writers[0].write(writer, instance);
				for (int i = 1; i < writers.length; i++) {
					if (writer.size() != pos || writer.flushed() != flushed) {
						writer.writeByte(JsonWriter.COMMA);
						pos = writer.size();
						flushed = writer.flushed();
					}
					writers[i].write(writer, instance);
				}
			}
			writer.writeByte(JsonWriter.OBJECT_END);
		}
	}
}
