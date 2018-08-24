package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonValue;

@CompiledJson
public enum EnumWithInvalidCustomConstantName3 {
	FIRST("a"),
	SECOND("b");

	@JsonValue
	public final String str;

	EnumWithInvalidCustomConstantName3(String str) {
		this.str = str;
	}

	@JsonValue
	public String getStr() {
		return str;
	}
}
