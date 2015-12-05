package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.fasterxml.jackson.annotation.JsonIgnore;

@CompiledJson
public class IgnoredProperty {
	private char prop;

	@JsonIgnore
	public char getProp() {
		return prop;
	}

	public void setProp(char value) {
		prop = value;
	}
}
