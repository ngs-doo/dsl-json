package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonObject;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;

@CompiledJson
public class ValidReferenceJsonObject {
	public ImplProper proper;

	public static class ImplProper implements JsonObject {

		@Override
		public void serialize(JsonWriter writer, boolean minimal) {
		}
		public static final JsonReader.ReadJsonObject<ImplProper> JSON_READER = new JsonReader.ReadJsonObject<ImplProper>() {
			@Override
			public ImplProper deserialize(JsonReader reader) throws IOException {
				return null;
			}
		};
	}
}
