package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson
public class SerializationMatch {
	@JsonAttribute(hashMatch = true)
	public String hash;
	@JsonAttribute(hashMatch = false)
	public String full;
	public String def;
}
