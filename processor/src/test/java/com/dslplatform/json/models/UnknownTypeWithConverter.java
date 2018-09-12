package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.ObjectAnalyzer;

import java.util.List;

@CompiledJson
public class UnknownTypeWithConverter {
	public String key = "abc";
	public Object value = 42;
	public List<Object> list;
	public Generic<Object> generic;

	@JsonConverter(target = Object.class)
	public static class WhitelistObjectConversion {
		public static final JsonReader.ReadObject<Object> JSON_READER = ObjectAnalyzer.Runtime.JSON_READER;
		public static final JsonWriter.WriteObject<Object> JSON_WRITER = ObjectAnalyzer.Runtime.JSON_WRITER;
	}

	public static class Generic<T> {
		public T property;
	}
}