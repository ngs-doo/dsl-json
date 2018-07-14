package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;

public interface FormatConverter<T> extends JsonWriter.WriteObject<T>, JsonReader.ReadObject<T> {
	T readContent(JsonReader reader) throws IOException;
	boolean writeContentMinimal(JsonWriter writer, T instance);
	void writeContentFull(JsonWriter writer, T instance);
}