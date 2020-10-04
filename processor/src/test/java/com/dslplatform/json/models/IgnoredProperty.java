package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.fasterxml.jackson.annotation.JsonIgnore;

@CompiledJson
public class IgnoredProperty {
	private char prop;

	@JsonIgnore
	public byte field1;

	@JsonAttribute(ignore = true)
	public char field2;

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

	@JsonAttribute(ignore = true)
	public String ignored;
	public String getIgnored() { return ignored; }
	public void setIgnored(String value) { ignored = value; }
}
