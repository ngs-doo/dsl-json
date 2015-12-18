package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.fasterxml.jackson.annotation.JsonIgnore;

@CompiledJson
public class IgnoredProperty {
	private char prop;

	@JsonIgnore
	public byte field;

	public final String name;

	public IgnoredProperty() {
		this.name = "";
	}

	@JsonIgnore
	public char getProp() {
		return prop;
	}

	public void setProp(char value) {
		prop = value;
	}
}
