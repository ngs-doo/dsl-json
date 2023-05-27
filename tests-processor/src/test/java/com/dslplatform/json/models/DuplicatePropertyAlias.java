package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.google.gson.annotations.SerializedName;

@CompiledJson
public class DuplicatePropertyAlias {
	@SerializedName("prop")
	public int num;
	public String prop;
}
