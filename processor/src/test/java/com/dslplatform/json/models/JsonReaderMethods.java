package com.dslplatform.json.models;

import com.dslplatform.json.*;

import java.io.IOException;

@CompiledJson
public class JsonReaderMethods {
	public ImplProper1 proper1;
	@JsonAttribute(converter = ImplProper2.class)
	public ImplProper2 proper2;
	public ImplProper3 proper3;
	public ImplProper4 proper4;

	@JsonConverter(target = ImplProper1.class)
	public static class ImplProper1 {

		public static final JsonReader.ReadObject<ImplProper1> JSON_READER = new JsonReader.ReadObject<ImplProper1>() {
			@Nullable
			@Override
			public ImplProper1 read(JsonReader reader) throws IOException {
				return null;
			}
		};
		public static final JsonWriter.WriteObject<ImplProper1> JSON_WRITER = new JsonWriter.WriteObject<ImplProper1>() {
			@Override
			public void write(JsonWriter writer, @Nullable ImplProper1 inst) {
			}
		};
	}
	public static class ImplProper2 {
		public static JsonReader.ReadObject<ImplProper2> JSON_READER() {
			return null;
		}
		public static JsonWriter.WriteObject<ImplProper2> JSON_WRITER() {
			return null;
		}
	}
	@JsonConverter(target = ImplProper3.class)
	public static class ImplProper3 {
		public static JsonReader.ReadObject<ImplProper3> getJSON_READER() {
			return null;
		}
		public static JsonWriter.WriteObject<ImplProper3> getJSON_WRITER() {
			return null;
		}
	}

	@JsonConverter(target = ImplProper4.class)
	public static class ImplProper4 {

		public static final JsonReader.ReadObject<ImplProper4> JSON_READER = new JsonReader.ReadObject<ImplProper4>() {
			@Nullable
			@Override
			public ImplProper4 read(JsonReader reader) throws IOException {
				return null;
			}
		};
		public static JsonWriter.WriteObject<ImplProper4> getJSON_WRITER() {
			return null;
		}
	}
}
