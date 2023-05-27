package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.processor.MinifiedNaming;
import com.google.gson.annotations.SerializedName;

@CompiledJson(namingStrategy = MinifiedNaming.class)
public class MinifiedProperties {
	public int width;
	public int height;
	public String name;
	@SerializedName("n")
	public int customNumber;
}
