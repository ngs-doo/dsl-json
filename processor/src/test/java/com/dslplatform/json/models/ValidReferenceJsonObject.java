package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;

@CompiledJson
public class ValidReferenceJsonObject {
	public ImplProper proper;

	public static class ImplProper implements JsonObject {

		@Override
		public void serialize(JsonWriter writer, boolean minimal) {
		}
		public static final JsonReader.ReadJsonObject<ImplProper> JSON_READER = new JsonReader.ReadJsonObject<ImplProper>() {
			@Nullable
			@Override
			public ImplProper deserialize(JsonReader reader) throws IOException {
				return null;
			}
		};
	}
}
