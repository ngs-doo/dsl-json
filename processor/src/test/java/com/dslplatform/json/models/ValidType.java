package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.fasterxml.jackson.annotation.JsonProperty;

@CompiledJson
public class ValidType {
	private int prop;

	@JsonProperty("test")
	public int getProp() {
		return prop;
	}

	public void setProp(int value) {
		prop = value;
	}
}
