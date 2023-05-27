package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonValue;

@CompiledJson
public enum EnumWithCustomConstantName2 {
	FIRST("a"),
	SECOND("b");

	private final String str;

	EnumWithCustomConstantName2(String str) {
		this.str = str;
	}

	@JsonValue
	public String getStr() {
		return str;
	}
}
