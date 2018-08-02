package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CompiledJson
public class ValidReferenceJsonObject {
	public ImplProper proper;
	public List<ImplProper> list;

	public static class ImplProper implements JsonObject {
		private ImplProper() {}

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
