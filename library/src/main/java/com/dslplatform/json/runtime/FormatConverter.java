package com.dslplatform.json.runtime;

import com.dslplatform.json.*;

import java.io.IOException;

public interface FormatConverter<T> extends JsonWriter.WriteObject<T>, JsonReader.ReadObject<T> {
	@Nullable
	T readContent(JsonReader reader) throws IOException;
	<X extends ControlInfo> boolean writeContentControlled(JsonWriter writer, @Nullable T instance, JsonControls<X> controls);
	void writeContentFull(JsonWriter writer, @Nullable T instance);
}