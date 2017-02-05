package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.google.gson.annotations.SerializedName;

@CompiledJson(skipUnknown = false)
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

	@JsonAttribute(name = "x", alternativeNames = {"X", "old_prop"})
	public String getProp() {
		return prop;
	}

	public void setProp(String value) {
		prop = value;
	}
}
