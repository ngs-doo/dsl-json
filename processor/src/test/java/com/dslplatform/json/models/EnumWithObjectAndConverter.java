package com.dslplatform.json.models;

import com.dslplatform.json.*;
import com.dslplatform.json.runtime.ObjectAnalyzer;

@CompiledJson
public enum EnumWithObjectAndConverter {
	FIRST(10),
	SECOND(20);

	private final Object value;

	EnumWithObjectAndConverter(int value) {
		this.value = value;
	}

	@JsonValue
	public Object getValue() {
		return value;
	}

	@JsonConverter(target = Object.class)
	public static class WhitelistObjectConversion {
		public static final JsonReader.ReadObject<Object> JSON_READER = ObjectAnalyzer.Runtime.JSON_READER;
		public static final JsonWriter.WriteObject<Object> JSON_WRITER = ObjectAnalyzer.Runtime.JSON_WRITER;
	}

}
