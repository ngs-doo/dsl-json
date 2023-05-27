package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonValue;

@CompiledJson
public enum EnumWithInvalidCustomConstantName4 {
	@JsonValue
	FIRST("a"),
	SECOND("b");

	public final String str;

	EnumWithInvalidCustomConstantName4(String str) {
		this.str = str;
	}
}
