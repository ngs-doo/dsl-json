package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.google.gson.annotations.SerializedName;

@CompiledJson
public class PrivateField {
	@JsonAttribute
	private int num;
	public String prop;
}
