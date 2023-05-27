package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson
public class CustomCtorPropertyName {
	public int x;

	public CustomCtorPropertyName(@JsonAttribute(name = "x2") int x) {
		this.x = x;
	}
}
