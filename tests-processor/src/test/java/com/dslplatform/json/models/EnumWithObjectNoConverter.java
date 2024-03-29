package com.dslplatform.json.models;

import com.dslplatform.json.*;

@CompiledJson
public enum EnumWithObjectNoConverter {
	FIRST(10),
	SECOND(20);

	private final Object value;

	EnumWithObjectNoConverter(int value) {
		this.value = value;
	}

	@JsonValue
	public Object getValue() {
		return value;
	}
}
