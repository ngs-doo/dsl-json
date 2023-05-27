package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.google.gson.annotations.SerializedName;

@CompiledJson(namingStrategy = CompiledJson.class)
public class BadMinifiedProperties {
	public int width;
	public int height;
	public String name;
	@SerializedName("n")
	public int customNumber;
}
