package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

@CompiledJson
public class PropertyAlias {
	private int num;

	@SerializedName("y")
	public int getNum() {
		return num;
	}

	public void setNum(int value) {
		num = value;
	}

	private String prop;

	@JsonProperty("x")
	public String getProp() {
		return prop;
	}

	public void setProp(String value) {
		prop = value;
	}
}
