package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonValue;

@CompiledJson
public enum EnumWithCustomConstantName4 {
	FIRST(10),
	SECOND(20);

	private final int value;

	EnumWithCustomConstantName4(int value) {
		this.value = value;
	}

	@JsonValue
	public int getValue() {
		return value;
	}
}
