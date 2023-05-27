package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.Nullable;

import java.io.IOException;

public interface FormatConverter<T> extends JsonWriter.WriteObject<T>, JsonReader.ReadObject<T> {
	@Nullable
	T readContent(JsonReader reader) throws IOException;
	boolean writeContentMinimal(JsonWriter writer, @Nullable T instance);
	void writeContentFull(JsonWriter writer, @Nullable T instance);
}