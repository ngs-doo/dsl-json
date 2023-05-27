package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.google.gson.annotations.SerializedName;

@CompiledJson(onUnknown = CompiledJson.Behavior.FAIL)
public class PropertyAliasWithPrivateAnnotation {
	@SerializedName("y")
	private int num;

	public int getNum() {
		return num;
	}

	public void setNum(int value) {
		num = value;
	}

	@JsonAttribute(name = "x", alternativeNames = {"X", "old_prop"})
	private String prop;

	public String getProp() {
		return prop;
	}

	public void setProp(String value) {
		prop = value;
	}
}
