package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;

@CompiledJson
public class ReferenceJsonObject {
	public ImplProper proper;
	public ImplFailed1 failed1;
	public ImplFailed2 failed2;
	public ImplFailed3 failed3;

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
	public static class ImplFailed1 implements JsonObject {

		@Override
		public void serialize(JsonWriter writer, boolean minimal) {
		}
	}
	public static class ImplFailed2 implements JsonObject {

		@Override
		public void serialize(JsonWriter writer, boolean minimal) {
		}
		public final JsonReader.ReadJsonObject<ImplFailed2> JSON_READER = new JsonReader.ReadJsonObject<ImplFailed2>() {
			@Nullable
			@Override
			public ImplFailed2 deserialize(JsonReader reader) throws IOException {
				return null;
			}
		};
	}
	public static class ImplFailed3 implements JsonObject {

		@Override
		public void serialize(JsonWriter writer, boolean minimal) {
		}
		public static final JsonReader.ReadJsonObject<ImplFailed2> JSON_READER = new JsonReader.ReadJsonObject<ImplFailed2>() {
			@Nullable
			@Override
			public ImplFailed2 deserialize(JsonReader reader) throws IOException {
				return null;
			}
		};
	}
}
