package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonValue;

@CompiledJson
public enum EnumWithCustomConstantName3 {
	FIRST(10),
	SECOND(20);

	@JsonValue
	public final int value;

	EnumWithCustomConstantName3(int value) {
		this.value = value;
	}
}
