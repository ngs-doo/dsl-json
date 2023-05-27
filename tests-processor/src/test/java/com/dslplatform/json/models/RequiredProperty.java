package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.fasterxml.jackson.annotation.JsonIgnore;

@CompiledJson
public class RequiredProperty {
	@com.fasterxml.jackson.annotation.JsonProperty(required = true)
	public String field1;

	@JsonAttribute(mandatory = true)
	public String field2;

	@JsonAttribute(mandatory = false)
	public String field3;

	@JsonAttribute
	public String field4;
}
