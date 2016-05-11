package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public enum EnumWithArgs {
	FIRST(1, "a"),
	SECOND(2, "b");

	public final int num;
	public final String str;

	EnumWithArgs(int num, String str) {
		this.num = num;
		this.str = str;
	}
}
