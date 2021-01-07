package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.util.Map;

@CompiledJson
public class PrivateConverter {
	@JsonAttribute(converter = Privates.class)
	public Private[] privates;
	@JsonAttribute(converter = Privates2.class)
	public Private[][] privates2;
	@JsonAttribute(converter = PrivatesMap.class)
	public Map<String, Private> privates3;

	public static abstract class Privates {
		public static final JsonReader.ReadObject<Private[]> JSON_READER = new JsonReader.ReadObject<Private[]>() {
			public Private[] read(JsonReader reader) throws IOException {
				return null;
			}
		};
		public static final JsonWriter.WriteObject<Private[]> JSON_WRITER = new JsonWriter.WriteObject<Private[]>() {
			public void write(JsonWriter writer, Private[] value) {
			}
		};
	}

	public static abstract class Privates2 {
		public static final JsonReader.ReadObject<Private[][]> JSON_READER = new JsonReader.ReadObject<Private[][]>() {
			public Private[][] read(JsonReader reader) throws IOException {
				return null;
			}
		};
		public static final JsonWriter.WriteObject<Private[][]> JSON_WRITER = new JsonWriter.WriteObject<Private[][]>() {
			public void write(JsonWriter writer, Private[][] value) {
			}
		};
	}

	public static abstract class PrivatesMap {
		public static final JsonReader.ReadObject<Map<String, Private>> JSON_READER = new JsonReader.ReadObject<Map<String, Private>>() {
			public Map<String, Private> read(JsonReader reader) throws IOException {
				return null;
			}
		};
		public static final JsonWriter.WriteObject<Map<String, Private>> JSON_WRITER = new JsonWriter.WriteObject<Map<String, Private>>() {
			public void write(JsonWriter writer, Map<String, Private> value) {
			}
		};
	}

	private class Private {}
}
