package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public class ReferenceType {
	private ValidType prop;

	public ValidType getProp() {
		return prop;
	}

	public void setProp(ValidType value) {
		prop = value;
	}
}
