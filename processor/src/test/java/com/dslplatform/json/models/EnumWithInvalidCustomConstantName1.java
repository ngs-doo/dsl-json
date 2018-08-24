package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonValue;

@CompiledJson
public enum EnumWithInvalidCustomConstantName1 {
	FIRST("a"),
	SECOND("b");

	private final String str;

	EnumWithInvalidCustomConstantName1(String str) {
		this.str = str;
	}

	@JsonValue
	String getStr() {
		return str;
	}
}
