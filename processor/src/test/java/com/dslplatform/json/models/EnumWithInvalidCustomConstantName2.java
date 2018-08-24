package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonValue;

@CompiledJson
public enum EnumWithInvalidCustomConstantName2 {
	FIRST("a"),
	SECOND("b");

	@JsonValue
	public final Object str;

	EnumWithInvalidCustomConstantName2(Object str) {
		this.str = str;
	}
}
