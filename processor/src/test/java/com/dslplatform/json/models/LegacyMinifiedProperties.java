package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.google.gson.annotations.SerializedName;

@CompiledJson(minified = true)
public class LegacyMinifiedProperties {
	public int width;
	public int height;
	public String name;
	@SerializedName("n")
	public int customNumber;
}
