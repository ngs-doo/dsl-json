package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonValue;

@CompiledJson
public enum EnumWithCustomConstantName1 {
	FIRST("a"),
	SECOND("b");

	@JsonValue
	public final String str;

	EnumWithCustomConstantName1(String str) {
		this.str = str;
	}
}
